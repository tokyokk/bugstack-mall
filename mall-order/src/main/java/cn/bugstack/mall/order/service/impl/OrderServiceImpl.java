package cn.bugstack.mall.order.service.impl;

import cn.bugstack.common.exception.NotStockException;
import cn.bugstack.common.to.OrderTO;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;
import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.order.constant.OrderConstant;
import cn.bugstack.mall.order.dao.OrderDao;
import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.entity.OrderItemEntity;
import cn.bugstack.mall.order.enume.OrderStatusEnum;
import cn.bugstack.mall.order.feign.CartFeignService;
import cn.bugstack.mall.order.feign.MemberFeignService;
import cn.bugstack.mall.order.feign.ProductFeignService;
import cn.bugstack.mall.order.feign.WmsFeignService;
import cn.bugstack.mall.order.interceptor.LoginUserInterceptor;
import cn.bugstack.mall.order.service.OrderService;
import cn.bugstack.mall.order.to.OrderCreateTO;
import cn.bugstack.mall.order.vo.*;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final ThreadLocal<OrderSubmitVo> ORDER_CONFIRM_THREAD_LOCAL = new ThreadLocal<>();

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final WmsFeignService wmsFeignService;
    private final StringRedisTemplate redisTemplate;
    private final ProductFeignService productFeignService;
    private final OrderItemServiceImpl orderItemService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;

    public OrderServiceImpl(final MemberFeignService memberFeignService, final CartFeignService cartFeignService,
                            final ThreadPoolExecutor threadPoolExecutor, final WmsFeignService wmsFeignService,
                            final StringRedisTemplate redisTemplate, final ProductFeignService productFeignService,
                            final OrderItemServiceImpl orderItemService, final RabbitTemplate rabbitTemplate, OrderService orderService) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
        this.threadPoolExecutor = threadPoolExecutor;
        this.wmsFeignService = wmsFeignService;
        this.redisTemplate = redisTemplate;
        this.productFeignService = productFeignService;
        this.orderItemService = orderItemService;
        this.rabbitTemplate = rabbitTemplate;
        this.orderService = orderService;
    }

    @Override
    public PageUtils queryPage(final Map<String, Object> params) {
        final IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        final OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        final MemberResponseVO memberResponseVO = LoginUserInterceptor.LOGIN_USER.get();

        // 解决feign异步调用丢失请求信息的问题
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        final CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 因为是异步的查询所以需要重新设置
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.远程查询所有的收获地址列表
            final List<MemberAddressVO> addressList = memberFeignService.getAddress(memberResponseVO.getId());
            orderConfirmVO.setAddress(addressList);
        }, threadPoolExecutor);

        final CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.远程查询购物车所有选中的购物项
            final List<OrderItemVO> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVO.setItems(items);
            // feign在调用请求之前要构造一个请求，调用很多拦截器
        }, threadPoolExecutor).thenRunAsync(() -> {
            final List<OrderItemVO> items = orderConfirmVO.getItems();
            final List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
            final R r = wmsFeignService.getSkuHasStock(skuIds);
            final List<SkuStockVO> data = r.getData(new TypeReference<List<SkuStockVO>>() {
            });
            final Map<Long, Boolean> stockMap = data.stream().collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));
            orderConfirmVO.setStocks(stockMap);
        }, threadPoolExecutor);


        // 3.查询用户积分
        final Integer integration = memberResponseVO.getIntegration();
        orderConfirmVO.setIntegration(integration);

        // 4.其他数据自动计算

        // 5.订单防重令牌
        final String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVO.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).join();

        return orderConfirmVO;
    }

    // @GlobalTransactional // 高并发场景并不适用
    @Transactional(rollbackFor = Exception.class)
    @Override
    public SubmitOrderResponseVO submitOrder(final OrderSubmitVo orderSubmitVo) {
        ORDER_CONFIRM_THREAD_LOCAL.set(orderSubmitVo);
        final SubmitOrderResponseVO response = new SubmitOrderResponseVO();
        response.setCode(0);
        final MemberResponseVO memberResponseVO = LoginUserInterceptor.LOGIN_USER.get();
        // 1.验证令牌【令牌的对比和删除都要保证原子性】 0 删除失败 1 删除成功
        final String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        final String orderToken = orderSubmitVo.getOrderToken();

        // 原子验证令牌和删除令牌
        final Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId()),
                orderToken);

        if (result == 0L) {
            // 令牌验证失败
            response.setCode(1);
            return response;
        } else {
            // 1.创建订单,订单项信息，价格信息
            final OrderCreateTO order = createOrder();
            // 2.验价
            final BigDecimal payAmount = order.getOrder().getPayAmount();
            final BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 3.保存订单
                saveOrder(order);
                // 4.库存锁定
                final WareSkuLockVO wareSkuLockVO = new WareSkuLockVO();
                wareSkuLockVO.setOrderSn(order.getOrder().getOrderSn());
                final List<OrderItemVO> locks = order.getOrderItems().stream().map(item -> {
                    final OrderItemVO orderItemVO = new OrderItemVO();
                    orderItemVO.setSkuId(item.getSkuId());
                    orderItemVO.setCount(item.getSkuQuantity());
                    orderItemVO.setTitle(item.getSkuName());
                    return orderItemVO;
                }).collect(Collectors.toList());
                wareSkuLockVO.setLocks(locks);
                ORDER_CONFIRM_THREAD_LOCAL.remove();
                // 为了保证高并发，库存服务自己回滚，可以发消息给库存服务
                // 库存服务本身也可以使用自动解锁模式 消息队列
                final R r = wmsFeignService.orderLockStock(wareSkuLockVO);
                if (r.getCode() == 0) {
                    // 锁定成功
                    response.setOrder(order.getOrder());
                    // 订单创建成功发送消息给MQ
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                    return response;
                } else {
                    // 锁定失败，解锁库存
                    response.setCode(3);
                    throw new NotStockException(locks.stream().map(OrderItemVO::getSkuId).findFirst().get());
                }
            } else {
                response.setCode(2);
                return response;
            }
        }

        // String redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId());
        // if (orderToken != null && orderToken.equals(redisToken)) {
        //     redisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVO.getId());
        // }
    }

    @Override
    public OrderEntity getOrderStatusByOrderSn(String orderSn) {
        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 查询这个订单的最新状态
        OrderEntity orderInfo = this.getById(orderEntity.getId());
        if (orderInfo.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 关单
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(orderInfo.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);
            OrderTO orderTo = new OrderTO();
            BeanUtils.copyProperties(orderInfo, orderTo);
            // 发送MQ，通知库存解锁服务
            try {
                // todo：保证消息一定发送成功，每一个消息都可以做好日记记录（给数据库保存每一个消息的详情记录）
                // todo：定期扫描数据库中未发送成功的消息，重新发送
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                // todo：将每发送出去的消息进行重试操作重新发送
            }
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = orderService.getOrderStatusByOrderSn(orderSn);
        if (order != null) {
            List<OrderItemEntity> orderItemList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));

            // 找到第一项的skuName
            orderItemList.stream().findFirst().ifPresent(item -> {
                payVo.setSubject(item.getSkuName());
            });
            payVo.setOut_trade_no(order.getOrderSn());
            String orderTotalAmount = order.getTotalAmount().setScale(2, RoundingMode.UP).toString();
            payVo.setTotal_amount(orderTotalAmount);
            payVo.setBody("订单：" + order.getOrderSn() + "支付成功");
        }
        return payVo;
    }

    private void saveOrder(final OrderCreateTO order) {
        final OrderEntity orderEntity = order.getOrder();
        orderEntity.setDeleteStatus(0);
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        final List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTO createOrder() {
        final OrderCreateTO orderCreateTO = new OrderCreateTO();
        // 1.订单号
        final String orderSn = IdWorker.getTimeId();

        final OrderEntity orderEntity = buildOrderData(orderSn);

        // 2.获取所有订单项信息
        final List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 3.计算价格相关
        computePrice(orderEntity, Objects.requireNonNull(orderItemEntities));
        orderCreateTO.setOrder(orderEntity);
        orderCreateTO.setOrderItems(orderItemEntities);

        return orderCreateTO;
    }

    private void computePrice(final OrderEntity orderEntity, final List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal(BigInteger.ZERO);
        // 订单总额，叠加每一个订单项的金额
        BigDecimal coupon = new BigDecimal(BigInteger.ZERO);
        BigDecimal promotionAmount = new BigDecimal(BigInteger.ZERO);
        BigDecimal integrationAmount = new BigDecimal(BigInteger.ZERO);
        BigDecimal giftIntegration = new BigDecimal(BigInteger.ZERO);
        BigDecimal giftGrowth = new BigDecimal(BigInteger.ZERO);
        for (final OrderItemEntity item : orderItemEntities) {
            total = total.add(item.getRealAmount());
            coupon = coupon.add(item.getCouponAmount());
            promotionAmount = promotionAmount.add(item.getPromotionAmount());
            integrationAmount = integrationAmount.add(promotionAmount);
            giftIntegration = giftIntegration.add(new BigDecimal(item.getPromotionAmount().toString()));
            giftGrowth = giftGrowth.add(new BigDecimal(item.getGiftIntegration().toString()));

        }
        // 订单总额
        orderEntity.setTotalAmount(total);
        // 订单优惠金额
        final BigDecimal payAmount = total.add(orderEntity.getFreightAmount());
        orderEntity.setPayAmount(payAmount);
        orderEntity.setPromotionAmount(promotionAmount);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integrationAmount);

        // 设置积分等信息
        orderEntity.setIntegration(giftIntegration.intValue());
        orderEntity.setGrowth(giftGrowth.intValue());
        orderEntity.setDeleteStatus(0);
    }

    /**
     * 构建订单数据
     */
    private OrderEntity buildOrderData(final String orderSn) {
        final MemberResponseVO responseVO = LoginUserInterceptor.LOGIN_USER.get();
        final OrderEntity orderData = new OrderEntity();
        orderData.setOrderSn(orderSn);
        orderData.setMemberId(responseVO.getId());
        // 获取收获地址信息
        final OrderSubmitVo submitVo = ORDER_CONFIRM_THREAD_LOCAL.get();
        final R r = wmsFeignService.getFare(submitVo.getAddrId());
        final FareVO fareResp = r.getData(new TypeReference<FareVO>() {
        });
        // 运费信息
        orderData.setFreightAmount(fareResp.getFare());
        // 设置收货人信息
        orderData.setReceiverCity(fareResp.getAddress().getCity());
        orderData.setReceiverDetailAddress(fareResp.getAddress().getDetailAddress());
        orderData.setReceiverName(fareResp.getAddress().getName());
        orderData.setReceiverPhone(fareResp.getAddress().getPhone());
        orderData.setReceiverPostCode(fareResp.getAddress().getPostCode());
        orderData.setReceiverProvince(fareResp.getAddress().getProvince());
        orderData.setReceiverRegion(fareResp.getAddress().getRegion());

        // 设置订单状态
        orderData.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderData.setAutoConfirmDay(7);
        return orderData;
    }

    /**
     * 构建订单项数据
     */
    private List<OrderItemEntity> buildOrderItems(final String orderSn) {
        final List<OrderItemVO> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && !currentUserCartItems.isEmpty()) {
            final List<OrderItemEntity> orderItemList = currentUserCartItems.stream().map(cartItem -> {
                final OrderItemEntity orderItem = buildOrderItem(cartItem);
                orderItem.setOrderSn(orderSn);
                return orderItem;
            }).collect(Collectors.toList());
            return orderItemList;
        }
        return null;
    }

    /**
     * 构建某一个订单项数据
     */
    private OrderItemEntity buildOrderItem(final OrderItemVO cartItem) {
        final OrderItemEntity orderItem = new OrderItemEntity();
        // 1.订单信息：订单号
        // 2.SPU信息
        final Long skuId = cartItem.getSkuId();
        final R spuResult = productFeignService.getSpuInfoBySkuId(skuId);
        final SpuInfoVO data = spuResult.getData(new TypeReference<SpuInfoVO>() {
        });
        orderItem.setSpuId(data.getId());
        orderItem.setSpuName(data.getSpuName());
        orderItem.setSpuBrand(data.getBrandId().toString());
        orderItem.setCategoryId(data.getCatalogId());
        // 3.SKU信息
        orderItem.setSkuId(skuId);
        orderItem.setSkuName(cartItem.getTitle());
        orderItem.setSkuPic(cartItem.getImage());
        orderItem.setSkuPrice(cartItem.getPrice());
        orderItem.setSkuQuantity(cartItem.getCount());
        final String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItem.setSkuAttrsVals(skuAttr);
        // 4.优惠信息
        // 5.积分信息
        orderItem.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());
        orderItem.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount().toString())).intValue());

        // 订单项的价格信息
        orderItem.setPromotionAmount(new BigDecimal("0"));
        orderItem.setCouponAmount(new BigDecimal("0"));
        orderItem.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单想的实际支付金额，原价*数量
        final BigDecimal originPrice = cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount()));
        final BigDecimal realPrice = originPrice.subtract(orderItem.getPromotionAmount()).subtract(orderItem.getCouponAmount()).subtract(orderItem.getIntegrationAmount());
        orderItem.setRealAmount(realPrice);

        // 6.金额信息
        return orderItem;
    }

}
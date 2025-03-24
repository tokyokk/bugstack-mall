package cn.bugstack.mall.order.service.impl;

import cn.bugstack.common.to.SkuHasStockVO;
import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.order.feign.CartFeignService;
import cn.bugstack.mall.order.feign.MemberFeignService;
import cn.bugstack.mall.order.feign.WmsFeignService;
import cn.bugstack.mall.order.interceptor.LoginUserInterceptor;
import cn.bugstack.mall.order.vo.MemberAddressVO;
import cn.bugstack.mall.order.vo.OrderConfirmVO;
import cn.bugstack.mall.order.vo.OrderItemVO;
import cn.bugstack.mall.order.vo.SkuStockVO;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.order.dao.OrderDao;
import cn.bugstack.mall.order.entity.OrderEntity;
import cn.bugstack.mall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final WmsFeignService wmsFeignService;

    public OrderServiceImpl(MemberFeignService memberFeignService, CartFeignService cartFeignService, ThreadPoolExecutor threadPoolExecutor, WmsFeignService wmsFeignService) {
        this.memberFeignService = memberFeignService;
        this.cartFeignService = cartFeignService;
        this.threadPoolExecutor = threadPoolExecutor;
        this.wmsFeignService = wmsFeignService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        MemberResponseVO memberResponseVO = LoginUserInterceptor.loginUser.get();

        // 解决feign异步调用丢失请求信息的问题
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // 因为是异步的查询所以需要重新设置
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1.远程查询所有的收获地址列表
            List<MemberAddressVO> addressList = memberFeignService.getAddress(memberResponseVO.getId());
            orderConfirmVO.setAddress(addressList);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2.远程查询购物车所有选中的购物项
            List<OrderItemVO> items = cartFeignService.getCurrentUserCartItems();
            orderConfirmVO.setItems(items);
            // feign在调用请求之前要构造一个请求，调用很多拦截器
        }, threadPoolExecutor).thenRunAsync(()->{
            List<OrderItemVO> items = orderConfirmVO.getItems();
            List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
            R r = wmsFeignService.getSkuHasStock(skuIds);
            List<SkuStockVO> data = r.getData(new TypeReference<List<SkuStockVO>>() {
            });
            Map<Long, Boolean> stockMap = data.stream().collect(Collectors.toMap(SkuStockVO::getSkuId, SkuStockVO::getHasStock));
            orderConfirmVO.setStocks(stockMap);
        },threadPoolExecutor);


        // 3.查询用户积分
        Integer integration = memberResponseVO.getIntegration();
        orderConfirmVO.setIntegration(integration);

        // 4.其他数据自动计算

        // 5.订单防重令牌

        CompletableFuture.allOf(getAddressFuture, cartFuture).join();

        return  orderConfirmVO;
    }

}
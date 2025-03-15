package cn.bugstack.mall.mallcart.service.impl;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.mallcart.feign.ProductFeignService;
import cn.bugstack.mall.mallcart.interceptor.CartInterceptor;
import cn.bugstack.mall.mallcart.service.CartService;
import cn.bugstack.mall.mallcart.vo.CartItem;
import cn.bugstack.mall.mallcart.vo.SkuInfoVO;
import cn.bugstack.mall.mallcart.vo.UserInfoTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 21:04
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;

    private final ProductFeignService productFeignService;

    private final ThreadPoolExecutor threadPoolExecutor;


    public CartServiceImpl(final StringRedisTemplate redisTemplate, final ProductFeignService productFeignService, final ThreadPoolExecutor threadPoolExecutor) {
        this.redisTemplate = redisTemplate;
        this.productFeignService = productFeignService;
        this.threadPoolExecutor = threadPoolExecutor;
    }


    public static final String CART_PREFIX = "mall:cart:";

    @Override
    public CartItem addToCart(final Long skuId, final Integer num) {
        final BoundHashOperations<String, Object, Object> boundHashOps = getCartOps();
        // 2.添加新商品到购物车

        final Object res = boundHashOps.get(skuId.toString());
        final CartItem cartItem;
        if (StringUtils.isEmpty(res)) {
            // 购物车中没有该商品，添加到购物车
            cartItem = new CartItem();
            // 1. 远程查询要添加的商品的信息
            final CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                final R r = productFeignService.getSkuInfo(skuId);
                final SkuInfoVO skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVO>() {
                });

                cartItem.setCheck(true);
                cartItem.setSkuId(skuId);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setCount(num);
            }, threadPoolExecutor);

            // 3.远程查询sku的组合信息
            final CompletableFuture<Void> getSkuSaleAttrValuesTask = CompletableFuture.runAsync(() -> {
                final List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(skuSaleAttrValues);
            }, threadPoolExecutor);
            try {
                CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValuesTask).join();
            } catch (final Exception e) {
                log.error("远程调用失败：{}", e.getMessage());
            }
            final String cartJson = JSON.toJSONString(cartItem);
            boundHashOps.put(skuId.toString(), cartJson);
        } else {
            // 购物车中已有该商品，修改数量即可
            cartItem = JSON.parseObject((String) res, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            final String cartJson = JSON.toJSONString(cartItem);
            boundHashOps.put(skuId.toString(), cartJson);
        }
        return cartItem;
    }

    @Override
    public CartItem getCartItem(final Long skuId) {
        final BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        final String str = (String) cartOps.get(skuId.toString());
        return JSON.parseObject(str, CartItem.class);
    }

    /**
     * 获取我们要操作的购物车
     *
     * @return {@link BoundHashOperations }<{@link String }, {@link Object }, {@link Object }>
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        final UserInfoTO userInfoTo = CartInterceptor.USERINFO_THREAD_LOCAL.get();
        final String cartKey;
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }

        return redisTemplate.boundHashOps(cartKey);
    }
}

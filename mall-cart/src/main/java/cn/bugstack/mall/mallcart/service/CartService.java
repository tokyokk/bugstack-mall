package cn.bugstack.mall.mallcart.service;

import cn.bugstack.mall.mallcart.vo.Cart;
import cn.bugstack.mall.mallcart.vo.CartItem;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 18:16
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public interface CartService {

    /**
     * 添加商品到购物车
     *
     * @param skuId 商品ID
     * @param num   商品数量
     * @return CartItem
     */
    CartItem addToCart(Long skuId, Integer num);

    /**
     * 获取购物车中某个商品
     *
     * @param skuId 商品ID
     * @return CartItem
     */
    CartItem getCartItem(Long skuId);

    Cart getCart();

    /**
     * 清空购物车
     *
     * @param cartkey 购物车key
     */
    void clearCart(String cartkey);
}

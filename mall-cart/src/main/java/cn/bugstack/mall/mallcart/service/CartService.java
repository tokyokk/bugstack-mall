package cn.bugstack.mall.mallcart.service;

import cn.bugstack.mall.mallcart.vo.Cart;
import cn.bugstack.mall.mallcart.vo.CartItem;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 18:16
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
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

    /**
     * 获取购物车
     *
     * @return Cart
     */
    Cart getCart();

    /**
     * 清空购物车
     *
     * @param cartkey 购物车key
     */
    void clearCart(String cartkey);

    /**
     * 修改购物车商品选中状态
     *
     * @param skuId 商品ID
     * @param check 选中状态
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物车商品数量
     *
     * @param skuId 商品ID
     * @param num   商品数量
     */
    void changeCountItem(Long skuId, Integer num);

    /**
     * 删除购物车商品
     *
     * @param skuId 商品ID
     */
    void deleteItem(Long skuId);

    List<CartItem> getUserCartItems();
}

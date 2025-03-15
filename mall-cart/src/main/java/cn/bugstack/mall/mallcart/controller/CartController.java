package cn.bugstack.mall.mallcart.controller;

import cn.bugstack.mall.mallcart.interceptor.CartInterceptor;
import cn.bugstack.mall.mallcart.service.CartService;
import cn.bugstack.mall.mallcart.vo.CartItem;
import cn.bugstack.mall.mallcart.vo.UserInfoTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/15 18:18
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    /**
     * @param session 浏览器有一个user-key：标识用户身份，一个月后过期
     *                如果第一次使用jd购物车功能，都会给一个user-key临时的用户身份
     *                浏览器以后保存，每次访问都会带上这个cookie
     *                如果登录：session存在。如果没登录：按照cookie的user-key来取身份
     * @return {@link String }
     */
    @GetMapping("/cart.html")
    public String cart(final HttpSession session) {
        // 1.快速得到用户信息
        final UserInfoTO userInfoTO = CartInterceptor.USERINFO_THREAD_LOCAL.get();
        return "cartList";
    }

    /**
     * 跳转到购物车成功页面
     *
     * @param skuId skuId
     * @param model 模型
     * @return {@link String }
     */
    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") final Long skuId, final Model model) {
        // 重定向到成功页面，再次查询购物车数据
        final CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    /**
     * 将商品添加到购物车
     * <p>
     * redirectAttributes.addFlashAttribute() ：将数据放在session中，可以在页面中取出，但是只能取一次
     * redirectAttributes.addAttribute()：将数据放在url中，在页面中取出
     *
     * @return {@link String }
     */
    @GetMapping("addToCart")
    public String addToCart(@RequestParam("skuId") final Long skuId,
                            @RequestParam("num") final Integer num,
                            final RedirectAttributes redirectAttributes) {
        cartService.addToCart(skuId, num);
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.mall.com/addToCartSuccess.html";
    }
}

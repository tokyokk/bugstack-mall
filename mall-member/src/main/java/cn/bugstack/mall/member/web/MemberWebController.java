package cn.bugstack.mall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/17 00:21
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class MemberWebController {

    @GetMapping("memberOrder.html")
    public String memberOrderPage() {
        // 查询当登录的用户订单列表数据
        return "orderList";
    }
}

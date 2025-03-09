package cn.bugstack.mall.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/8 19:47
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class LoginController {

    @GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage() {
        return "reg";
    }
}

package cn.bugstack.mall.member.web;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.expression.Maps;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/17 00:21
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class MemberWebController {

    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping("memberOrder.html")
    public String memberOrderPage(@RequestParam("pageNum") Integer pageNum, Model model) {
        // 获取支付宝给我们传来的所以请求数据
        // request 验证签名，如果正确可以去修改。

        // 查询当登录的用户订单列表数据
        Map<String, Object> page  = new HashMap<>();
        page.put("page", pageNum.toString());
        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders", r);
        return "orderList";
    }
}

package cn.bugstack.mall.order.web;

import cn.bugstack.mall.order.service.impl.OrderServiceImpl;
import cn.bugstack.mall.order.vo.OrderConfirmVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 19:29
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class OrderWebController {

    private final OrderServiceImpl orderService;

    public OrderWebController(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @GetMapping("toTrade")
    public String toTrade(Model model) {
        OrderConfirmVO orderConfirmVO = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVO);
        return "confirm";
    }
}

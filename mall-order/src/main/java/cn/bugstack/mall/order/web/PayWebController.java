package cn.bugstack.mall.order.web;

import cn.bugstack.mall.order.config.AlipayTemplate;
import cn.bugstack.mall.order.service.OrderService;
import cn.bugstack.mall.order.vo.PayVo;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/16 21:07
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class PayWebController {

    @Resource
    private AlipayTemplate alipayTemplate;

    @Resource
    private OrderService orderService;

    /**
     * 1、将支付页让浏览器展示
     * 2、支付成功以后，我们要跳到用户的订单列表页
     *
     * @param orderSn 订单号
     * @return
     */
    @GetMapping(value = "payOrder", produces = "text/html")
    @SneakyThrows
    @ResponseBody
    public String payOrder(@RequestParam("orderSn") String orderSn) {
        // 获取当前订单的支付信息
        PayVo payVo = orderService.getOrderPay(orderSn);

        // 返回的是一个页面，直接交给浏览器就可以
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}

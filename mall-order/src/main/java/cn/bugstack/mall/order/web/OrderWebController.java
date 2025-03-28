package cn.bugstack.mall.order.web;

import cn.bugstack.mall.order.service.impl.OrderServiceImpl;
import cn.bugstack.mall.order.vo.OrderConfirmVO;
import cn.bugstack.mall.order.vo.OrderSubmitVo;
import cn.bugstack.mall.order.vo.SubmitOrderResponseVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    public OrderWebController(final OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @GetMapping("toTrade")
    public String toTrade(final Model model) {
        final OrderConfirmVO orderConfirmVO = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVO);
        return "confirm";
    }

    @PostMapping("submitOrder")
    public String submitOrder(final OrderSubmitVo orderSubmitVo, final Model model, final RedirectAttributes redirectAttributes) {
        final SubmitOrderResponseVO responseVO = orderService.submitOrder(orderSubmitVo);
        if (responseVO.getCode() == 0) {
            // 下单成功取支付页
            model.addAttribute("orderSubmitData", responseVO);
            return "pay";
        } else {
            String msg = "下单失败：";
            switch (responseVO.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新在提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确定后再提交";
                    break;
                case 3:
                    msg += "库存锁定失败，商品库存不足";
                    break;
                default:
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.mall.com/toTrade";
        }
    }
}

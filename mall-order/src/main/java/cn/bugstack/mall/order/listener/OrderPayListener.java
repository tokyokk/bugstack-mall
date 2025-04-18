package cn.bugstack.mall.order.listener;


import cn.bugstack.mall.order.config.AlipayTemplate;
import cn.bugstack.mall.order.service.OrderService;
import cn.bugstack.mall.order.vo.PayAsyncVo;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/18 00:03
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@RestController
public class OrderPayListener {

    private final OrderService orderService;

    private final AlipayTemplate alipayTemplate;

    public OrderPayListener(OrderService orderService, AlipayTemplate alipayTemplate) {
        this.orderService = orderService;
        this.alipayTemplate = alipayTemplate;
    }

    @PostMapping("/payed/notify")
    public String handlerAlipay(HttpServletRequest request, PayAsyncVo payAsyncVo) throws AlipayApiException {
        System.out.println("收到支付宝异步通知******************");
        // 只要收到支付宝的异步通知，返回 success 支付宝便不再通知
        // 获取支付宝POST过来反馈信息
        //TODO 需要验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(), alipayTemplate.getSign_type()); //调用SDK验证签名

        if (signVerified){
            System.out.println("支付宝异步通知验签成功");
            //修改订单状态
            String result = orderService.handlePayResult(payAsyncVo);
            return result;
        }else {
            System.out.println("支付宝异步通知验签失败");
            return "error";
        }
    }
}

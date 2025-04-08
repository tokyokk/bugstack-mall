package cn.bugstack.mall.ware.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/8 22:07
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient(value = "mall-order")
public interface OrderFeignService {

    /**
     * 根据订单号查询订单状态
     */
    @GetMapping("order/order/status/{orderSn}")
    public R getOrderStatusByOrderSn(@PathVariable("orderSn") String orderSn);
}

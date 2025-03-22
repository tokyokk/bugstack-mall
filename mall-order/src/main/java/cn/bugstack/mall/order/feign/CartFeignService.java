package cn.bugstack.mall.order.feign;

import cn.bugstack.mall.order.vo.OrderItemVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 23:12
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-cart")
public interface CartFeignService {

    /**
     * 获取当前用户的购物车项
     */
    @GetMapping("/currentUserCartItems")
    public List<OrderItemVO> getCurrentUserCartItems();
}

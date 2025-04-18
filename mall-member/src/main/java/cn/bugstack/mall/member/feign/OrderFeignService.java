package cn.bugstack.mall.member.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/17 22:17
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient(name = "mall-order")
public interface OrderFeignService {

    /**
     * 分页查询当前用户的订单列表，包括订单详情信息。
     *
     * @param params 请求参数，可以包含分页参数、查询条件等
     * @return 包含订单列表和分页信息的响应对象
     */
    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);
}

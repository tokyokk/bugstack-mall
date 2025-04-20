package cn.bugstack.mall.seckill.feign;

import cn.bugstack.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author micro, 微信：yykk、
 * @description 远程调用sms优惠服务
 * @date 2025/4/20 15:37
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latest3DaysSession")
    public R getLatest3DaysSession();
}

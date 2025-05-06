package cn.bugstack.mall.product.feign.fallbcak;

import cn.bugstack.common.exception.BizCodeEnum;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/5/6 21:24
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Component
@Slf4j
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSeckillSkuInfo(Long skuId) {
        log.error("请求商品秒杀信息失败：{}", skuId);
        return R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
    }
}

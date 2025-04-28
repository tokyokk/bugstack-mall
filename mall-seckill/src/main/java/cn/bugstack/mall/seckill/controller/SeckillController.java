package cn.bugstack.mall.seckill.controller;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.seckill.service.SeckillService;
import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/26 01:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@RestController
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @GetMapping("/findCurrentSeckillSkus")
    public R findCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = seckillService.findCurrentSeckillSkus();
        return R.ok().setData(vos);
    }
}

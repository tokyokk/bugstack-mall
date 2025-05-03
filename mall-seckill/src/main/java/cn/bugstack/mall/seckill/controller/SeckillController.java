package cn.bugstack.mall.seckill.controller;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.seckill.service.SeckillService;
import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/26 01:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class SeckillController {

    private final SeckillService seckillService;

    public SeckillController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @ResponseBody
    @GetMapping("/findCurrentSeckillSkus")
    public R findCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> vos = seckillService.findCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo redisTo = seckillService.getSeckillSkuInfo(skuId);
        return R.ok().setData(redisTo);
    }

    @GetMapping("kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num, Model model) {
        // 1、判断是否登录
        String orderSn = seckillService.seckill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}

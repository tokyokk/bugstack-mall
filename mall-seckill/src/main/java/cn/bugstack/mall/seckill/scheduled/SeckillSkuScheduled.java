package cn.bugstack.mall.seckill.scheduled;

import cn.bugstack.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author micro, 微信：yykk、
 * @description 秒杀商品的定时上架
 *                  每天晚上3点；上架最近三天需要秒杀的商品    当天ee:09:00 - 23:59:59
 *                                                       明天09:00:00 - 23:59:59
 *                                                       后天00:00:0e - 23:59:59
 * @date 2025/4/19 20:35
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Service
@Slf4j
public class SeckillSkuScheduled {

    private final SeckillService seckillService;

    public SeckillSkuScheduled(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    /**
     * 1、幂等信处理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 1.重复上架无需处理
        seckillService.uploadSeckillSkuLatest3Days();
    }
}

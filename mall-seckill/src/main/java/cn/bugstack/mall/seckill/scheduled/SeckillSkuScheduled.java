package cn.bugstack.mall.seckill.scheduled;

import cn.bugstack.mall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    private final RedissonClient redissonClient;

    public static final String UPLOAD_SECKILL_LOCK = "seckill:lock";

    public SeckillSkuScheduled(SeckillService seckillService, RedissonClient redissonClient) {
        this.seckillService = seckillService;
        this.redissonClient = redissonClient;
    }

    /**
     * 1、幂等信处理
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        // 1.重复上架无需处理
        log.info("定时上架最近三天需要秒杀的商品");
        // 分布式锁解决重复上架问题
        RLock lock = redissonClient.getLock(UPLOAD_SECKILL_LOCK);
        try {
            boolean rLock = lock.tryLock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLatest3Days();
        } catch (InterruptedException e) {
            log.error("上架秒杀商品异常：{},错误信息：{}", e, e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread() && lock.isLocked()) {
                lock.unlock();
            }
        }
    }
}

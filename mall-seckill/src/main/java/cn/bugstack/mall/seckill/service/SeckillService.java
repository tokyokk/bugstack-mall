package cn.bugstack.mall.seckill.service;

import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/20 15:34
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public interface SeckillService {

    /**
     * 定时任务：从数据库中查询3天内的秒杀商品信息，写入Redis
     */
    void uploadSeckillSkuLatest3Days();

    /**
     * 获取当前秒杀商品信息
     */
    List<SeckillSkuRedisTo> findCurrentSeckillSkus();
}

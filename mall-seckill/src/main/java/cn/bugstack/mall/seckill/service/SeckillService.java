package cn.bugstack.mall.seckill.service;

import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;

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
     *
     * @return 当前秒杀商品信息
     */
    List<SeckillSkuRedisTo> findCurrentSeckillSkus();


    /**
     * 获取指定商品的秒杀信息
     *
     * @param skuId 商品ID
     * @return 秒杀商品信息
     */
    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);

    /**
     * 秒杀商品
     *
     * @param killId 秒杀场次ID
     * @param key    商品key
     * @param num    秒杀数量
     * @return 订单号
     */
    String seckill(String killId, String key, Integer num);
}

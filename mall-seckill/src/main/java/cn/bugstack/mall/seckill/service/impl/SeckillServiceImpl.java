package cn.bugstack.mall.seckill.service.impl;

import cn.bugstack.common.FeignCodeEnum;
import cn.bugstack.common.constant.CharacterConstant;
import cn.bugstack.common.constant.ProductConstant;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.seckill.feign.CouponFeignService;
import cn.bugstack.mall.seckill.feign.ProductFeignService;
import cn.bugstack.mall.seckill.service.SeckillService;
import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;
import cn.bugstack.mall.seckill.vo.SeckillSessionsWithSkus;
import cn.bugstack.mall.seckill.vo.SkuInfoVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/20 15:34
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final CouponFeignService couponFeignService;

    private final StringRedisTemplate redisTemplate;

    private final ProductFeignService productFeignService;

    private final RedissonClient redissonClient;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    public static final String SKU_CACHE_PREFIX = "seckill:skus";

    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock"; // +商品随机吗

    public SeckillServiceImpl(CouponFeignService couponFeignService, StringRedisTemplate redisTemplate, ProductFeignService productFeignService, RedissonClient redissonClient) {
        this.couponFeignService = couponFeignService;
        this.redisTemplate = redisTemplate;
        this.productFeignService = productFeignService;
        this.redissonClient = redissonClient;
    }

    @Override
    public void uploadSeckillSkuLatest3Days() {
        // 1.扫描最近三天需要参加的活动
        R r = couponFeignService.getLatest3DaysSession();
        if (Objects.equals(r.getCode(), FeignCodeEnum.SUCCESS.getCode())) {
            // 上架商品
            List<SeckillSessionsWithSkus> data = r.getData(new TypeReference<List<SeckillSessionsWithSkus>>() {
            });
            // 缓存到redis
            // 1、缓存活动的信息
            saveSessionInfos(data);
            // 2、缓存活动的关联的商品信息
            saveSkuInfos(data);
        }
    }

    private void saveSessionInfos(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + CharacterConstant.UNDERLINE + endTime;
            if (redisTemplate.hasKey(key)) {
                return;
            }

            List<String> skuIds = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + CharacterConstant.UNDERLINE + item.getSkuId().toString()).collect(Collectors.toList());
            // 缓存活动信息
            redisTemplate.opsForList().leftPushAll(key, skuIds);
        });
    }

    private void saveSkuInfos(List<SeckillSessionsWithSkus> sessionData) {
        sessionData.forEach(session -> {
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
            session.getRelationSkus().forEach(sku -> {
                // 4、设置秒杀的随机吗：解决秒杀被恶意刷单的问题，也是防止超卖的问题 seckill:skus:skuId=1&key=随机码
                String token = UUID.randomUUID().toString().replaceAll(CharacterConstant.HYPHEN, "");
                if (Boolean.TRUE.equals(hashOps.hasKey(sku.getPromotionSessionId() + CharacterConstant.UNDERLINE + sku.getSkuId().toString()))) {
                    // 缓存商品信息
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();
                    // 1、Sku的基本数据
                    R r = productFeignService.getSkuInfo(sku.getSkuId());
                    if (Objects.equals(r.getCode(), FeignCodeEnum.SUCCESS.getCode())) {
                        SkuInfoVO skuInfo = r.getData(ProductConstant.ResultEnum.SKU_INFO.getValue(), new TypeReference<SkuInfoVO>() {
                        });
                        seckillSkuRedisTo.setSkuInfo(skuInfo);
                    }

                    // 2、sku的秒杀信息
                    BeanUtils.copyProperties(sku, seckillSkuRedisTo);

                    // 3、设置当前商品秒杀的时间信息
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    seckillSkuRedisTo.setRandomCode(token);

                    hashOps.put(sku.getPromotionSessionId() + CharacterConstant.UNDERLINE + sku.getSkuId().toString(), JSON.toJSONString(seckillSkuRedisTo));

                    // 如果当前这个场次的商品的库存信息已经上架就不需要上架
                    // 5、使用库存作为分布式信号量，主要作用：限流
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 设置信号量，设置库存数量，商品可以秒杀的数量
                    semaphore.trySetPermits(sku.getSeckillCount().intValue());
                }
            });
        });
    }
}

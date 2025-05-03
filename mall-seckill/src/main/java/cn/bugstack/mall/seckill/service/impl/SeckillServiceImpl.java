package cn.bugstack.mall.seckill.service.impl;

import cn.bugstack.common.FeignCodeEnum;
import cn.bugstack.common.constant.CharacterConstant;
import cn.bugstack.common.constant.ProductConstant;
import cn.bugstack.common.to.mq.SeckillOrderTo;
import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.seckill.feign.CouponFeignService;
import cn.bugstack.mall.seckill.feign.ProductFeignService;
import cn.bugstack.mall.seckill.interceptor.LoginUserInterceptor;
import cn.bugstack.mall.seckill.service.SeckillService;
import cn.bugstack.mall.seckill.to.SeckillSkuRedisTo;
import cn.bugstack.mall.seckill.vo.SeckillSessionsWithSkus;
import cn.bugstack.mall.seckill.vo.SkuInfoVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
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

    private final RabbitTemplate rabbitTemplate;

    public static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";

    public static final String SKU_CACHE_PREFIX = "seckill:skus";

    public static final String SKU_STOCK_SEMAPHORE = "seckill:stock"; // +商品随机吗

    public SeckillServiceImpl(CouponFeignService couponFeignService, StringRedisTemplate redisTemplate, ProductFeignService productFeignService, RedissonClient redissonClient, RabbitTemplate rabbitTemplate) {
        this.couponFeignService = couponFeignService;
        this.redisTemplate = redisTemplate;
        this.productFeignService = productFeignService;
        this.redissonClient = redissonClient;
        this.rabbitTemplate = rabbitTemplate;
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

    @Override
    public List<SeckillSkuRedisTo> findCurrentSeckillSkus() {
        // 1、获取当前时间
        long nowTime = System.currentTimeMillis();
        // 2、获取当前时间对应的活动信息
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }
        // 3、判断当前时间是否在活动时间内，如果在则获取到活动的商品信息
        for (String key : Collections.unmodifiableSet(keys)) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            if (nowTime >= startTime && nowTime <= endTime) {
                // 4、获取到活动的商品信息
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
                List<String> skuIds = redisTemplate.opsForList().range(key, 0, -1);
                // 这里需要注意因为秒杀已经开始了所以需要返回随机吗，否则是需要将随机吗去除的！
                return Objects.requireNonNull(hashOps.multiGet(Objects.requireNonNull(skuIds))).stream().map(item -> JSON.parseObject(item, SeckillSkuRedisTo.class)).collect(Collectors.toList());
            }
            break;
        }
        return Collections.emptyList();
    }

    @Override
    public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {
        // 1、找到所有需要参与秒杀的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(hashOps.get(key), SeckillSkuRedisTo.class);
                    // 随机码
                    long currentTime = ZonedDateTime.now().toInstant().toEpochMilli();
                    if (currentTime >= Objects.requireNonNull(seckillSkuRedisTo).getStartTime() && currentTime <= seckillSkuRedisTo.getEndTime()) {
                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * todo：上架秒杀商品的时候，每一个数据都给定一个过期时间
     * todo：秒杀后续的流程，收货地址等信息
     * @param killId 秒杀场次ID
     * @param key    商品key
     * @param num    秒杀数量
     * @return
     */
    @Override
    public String seckill(String killId, String key, Integer num) {
        MemberResponseVO responseVO = LoginUserInterceptor.LOGIN_USER.get();
        // 1、登录校验已处理
        // 2、获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKU_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            // 校验合法性
            Long startTime = seckillSkuRedisTo.getStartTime();
            Long endTime = seckillSkuRedisTo.getEndTime();
            long nowTime = System.currentTimeMillis();

            long ttl = endTime - nowTime;
            // 1、校验时间的合法性
            if (nowTime >= startTime && nowTime <= endTime) {
                // 校验随机吗和商品id
                String randomCode = seckillSkuRedisTo.getRandomCode();
                String skuId = seckillSkuRedisTo.getPromotionId() + "_" + seckillSkuRedisTo.getSkuId();
                if (randomCode.equals(key) && skuId.equals(killId)) {
                    // 3、验证购买数量合法性
                    if (num <= seckillSkuRedisTo.getSeckillLimit()) {
                        // 4、验证这个人是否已经购买过，幂等性，只要秒杀成功，就去占位。userId_promotionId_skuId
                        String redisKey = responseVO.getId() + "_" + seckillSkuRedisTo.getPromotionId() + "_" + seckillSkuRedisTo.getSkuId();
                        // setnx 自动过期
                        Boolean absent = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (Boolean.TRUE.equals(absent)) {
                            // 占位成功，说明这个人从来没有买过
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean tryAcquire = semaphore.tryAcquire(num);
                            if (tryAcquire) {
                                // 秒杀成功
                                // 快速下单发送一个MQ消息
                                String timeId = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(timeId);
                                seckillOrderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
                                seckillOrderTo.setSkuId(seckillSkuRedisTo.getSkuId());
                                seckillOrderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
                                seckillOrderTo.setNum(num);
                                seckillOrderTo.setMemberId(responseVO.getId());

                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", seckillOrderTo);
                                return timeId;
                            }
                            return null;

                        } else {
                            // 说明已经买过了
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return null;
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

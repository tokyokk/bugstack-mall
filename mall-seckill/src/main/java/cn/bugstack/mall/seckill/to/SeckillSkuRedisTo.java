package cn.bugstack.mall.seckill.to;

import cn.bugstack.mall.seckill.vo.SkuInfoVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/20 19:20
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SeckillSkuRedisTo {

    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 商品秒杀随机码
     */
    private String randomCode;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    /**
     * sku的详细信息
     */
    private SkuInfoVO skuInfo;

    /**
     * 当前商品秒杀的开始时间
     */
    private Long startTime;

    /**
     * 当前商品秒杀的结束时间
     */
    private Long endTime;
}

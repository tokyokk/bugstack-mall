package cn.bugstack.mall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/20 19:01
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SeckillSessionsWithSkus {

    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;

    private List<SeckillSkuVO> relationSkus;
}

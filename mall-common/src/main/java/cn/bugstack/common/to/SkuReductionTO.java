package cn.bugstack.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/25 21:56
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class SkuReductionTO {

    private Long skuId;

    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}

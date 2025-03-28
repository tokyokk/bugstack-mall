package cn.bugstack.mall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/22 22:48
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class OrderItemVO implements Serializable {

    private static final long serialVersionUID = 3413527000289536800L;

    private Long skuId;

    private String title;

    private String image;

    private List<String> skuAttr;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    private BigDecimal weight;
}

package cn.bugstack.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/6 12:28
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
@ToString
public class SkuItemSaleAttrVO {

    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVO> attrValues;
}

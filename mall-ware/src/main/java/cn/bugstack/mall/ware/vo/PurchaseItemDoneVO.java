package cn.bugstack.mall.ware.vo;

import lombok.Data;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/1 18:09
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class PurchaseItemDoneVO {

    private Long itemId;
    private Integer status;
    private String reason;
}

package cn.bugstack.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/1 00:30
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class MergeVO {

    /**
     * 采购单id
     */
    private Long purchaseId;

    /**
     * 采购项id集合
     */
    private List<Long> items;
}

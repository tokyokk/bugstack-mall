package cn.bugstack.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/1 18:08
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class PurchaseDoneVO {

    /**
     * 采购单id
     */
    @NotNull
    private Long id;

    private List<PurchaseItemDoneVO> items;
}

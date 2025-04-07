package cn.bugstack.common.to.mq;

import lombok.Data;

import java.util.List;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/4/6 20:34
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class StockLockedTO {

    /**
     * 库存工作单id
     */
    private Long id;

    /**
     * 工作单详情单id
     */
    private StockDetailTO detail;
}

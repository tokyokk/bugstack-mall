package cn.bugstack.mall.ware.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/29 00:03
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Getter
@Setter
public class NotStockException extends RuntimeException{

    private Long skuId;

    public NotStockException(Long skuId) {
        super("商品id：" + skuId + "库存不足");
    }
}

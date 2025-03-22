package cn.bugstack.mall.product.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/2/23 21:25
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Data
public class AttrRespVO extends AttrVO{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}

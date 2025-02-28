package cn.bugstack.mall.product.entity;

import cn.bugstack.common.valid.InsertGroup;
import cn.bugstack.common.valid.ListValue;
import cn.bugstack.common.valid.UpdateGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须指定ID", groups = {UpdateGroup.class})
    @Null(message = "新增不能指定ID", groups = {InsertGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名不能为空", groups = {InsertGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty
    @URL(message = "品牌logo地址必须是一个合法的url地址")
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotEmpty
    @ListValue(vals = {0, 1})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(groups = {InsertGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {InsertGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotEmpty(groups = {InsertGroup.class})
    @Min(value = 0, message = "排序必须大于等于0", groups = {InsertGroup.class, UpdateGroup.class})
    private Integer sort;

}

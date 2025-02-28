/**
  * Copyright 2021 bejson.com 
  */
package cn.bugstack.mall.product.vo;

import lombok.Data;
import lombok.ToString;


/**
 * @author yaoxinjia
 */
@ToString
@Data
public class Attr {

    private Long attrId;
    private String attrName;
    private String attrValue;

}
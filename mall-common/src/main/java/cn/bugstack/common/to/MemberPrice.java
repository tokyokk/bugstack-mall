/**
  * Copyright 2021 bejson.com 
  */
package cn.bugstack.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yaoxinjia
 */
@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;


}
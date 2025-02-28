package cn.bugstack.mall.product.dao;

import cn.bugstack.mall.product.entity.AttrAttrgroupRelationEntity;
import cn.bugstack.mall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    void deleteBatchRelation(@Param("relationEntityList") List<AttrAttrgroupRelationEntity> relationEntityList);

}

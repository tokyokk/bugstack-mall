package cn.bugstack.mall.product.dao;

import cn.bugstack.mall.product.entity.AttrGroupEntity;
import cn.bugstack.mall.product.vo.SpuItemAttrGroupVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVO> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}

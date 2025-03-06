package cn.bugstack.mall.product.dao;

import cn.bugstack.mall.product.entity.SkuSaleAttrValueEntity;
import cn.bugstack.mall.product.vo.SkuItemSaleAttrVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttrVO> getSaleAttrsBySpuId(@Param("spuId") Long spuId);
}

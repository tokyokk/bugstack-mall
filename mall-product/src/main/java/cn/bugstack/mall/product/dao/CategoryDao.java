package cn.bugstack.mall.product.dao;

import cn.bugstack.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}

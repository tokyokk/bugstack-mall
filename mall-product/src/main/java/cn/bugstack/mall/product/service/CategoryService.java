package cn.bugstack.mall.product.service;

import cn.bugstack.mall.product.vo.CateLog2VO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询所有分类
     *
     * @return 分类集合
     */
    List<CategoryEntity> listWithTree();

    /**
     * 批量删除菜单
     *
     * @param list 菜单id集合
     * @return 删除结果
     */
    Boolean removeMenuByIds(List<Long> list);

    /**
     * 查找cateLogId的完整路径
     *
     * @param cateLogId 菜单id
     * @return 菜单路径
     */
    Long[] findCateLogPath(Long cateLogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<CateLog2VO>> getCatalogJson();
}


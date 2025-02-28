package cn.bugstack.mall.product.service.impl;

import cn.bugstack.mall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.CategoryDao;
import cn.bugstack.mall.product.entity.CategoryEntity;
import cn.bugstack.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);

        // 2. 找到所有的一级分类
        List<CategoryEntity> level1Menus = categoryEntityList.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildrens(menu, categoryEntityList));
                    return menu;
                }).sorted(
                        (menu1, menu2) -> {
                            // 菜单排序
                            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                        }
                ).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public Boolean removeMenuByIds(List<Long> list) {
        // todo 1。检查当前删除的菜单，是否被其他地方引用
        baseMapper.deleteBatchIds(list);
        return true;
    }

    @Override
    public Long[] findCateLogPath(Long cateLogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(cateLogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param cateLogId 当前菜单id
     * @param paths     路径
     * @return 菜单路径
     */
    private List<Long> findParentPath(Long cateLogId, List<Long> paths) {
        paths.add(cateLogId);
        CategoryEntity categoryEntity = getById(cateLogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity menu, List<CategoryEntity> categoryEntityList) {
        return categoryEntityList.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(menu.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildrens(categoryEntity, categoryEntityList));
            return categoryEntity;
        }).sorted(
                (menu1, menu2) -> {
                    // 菜单排序
                    return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
                }
        ).collect(Collectors.toList());
    }

}
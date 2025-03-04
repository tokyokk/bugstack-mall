package cn.bugstack.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.bugstack.mall.product.entity.BrandEntity;
import cn.bugstack.mall.product.vo.BrandVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.product.entity.CategoryBrandRelationEntity;
import cn.bugstack.mall.product.service.CategoryBrandRelationService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:47
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @RequestMapping(value = "/brands/list", method = RequestMethod.GET)
    public R relationBrandsList(@RequestParam(value = "catId", required = true) Long catId) {
        List<BrandEntity> vos = categoryBrandRelationService.getBrandsByCatId(catId);

        List<BrandVO> brandVOList = vos.stream().map(item -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(item.getBrandId());
            brandVO.setBrandName(item.getName());
            return brandVO;
        }).collect(Collectors.toList());

        return R.ok().put("data", brandVOList);
    }

    /**
     * 当前产品关联的所有分类列表
     */
    @RequestMapping(value = "/catelog/list", method = RequestMethod.GET)
    public R catelogList(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.listCatelogAndBrand(brandId);

        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDetails(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

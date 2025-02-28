package cn.bugstack.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.bugstack.mall.product.entity.AttrEntity;
import cn.bugstack.mall.product.service.AttrAttrgroupRelationService;
import cn.bugstack.mall.product.service.AttrService;
import cn.bugstack.mall.product.service.CategoryService;
import cn.bugstack.mall.product.vo.AttrGroupRelactionVO;
import cn.bugstack.mall.product.vo.AttrGroupWithAttrsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.product.entity.AttrGroupEntity;
import cn.bugstack.mall.product.service.AttrGroupService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;


/**
 * 属性分组
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:47
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 根据分类id查询分组及分组下关联的属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long cateLogId) {
        // 1.查询当前分类下的所有属性分组
        // 2.查出每个属性分组的所有属性
       List<AttrGroupWithAttrsVO> attrGroupWithAttrs =  attrGroupService.getAttrGroupWithAttrsByCateLogId(cateLogId);
       return R.ok().put("data", attrGroupWithAttrs);
    }

    /**
     * 添加分组与属性的关联
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelactionVO> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 获取当前分组关联的所有属性
     */
    @GetMapping("/{attrgroupId}/attr/relaction")
    public R attrRelation(@PathVariable("attrgroupId") Long attrGroupId) {
        List<AttrEntity> attrEntityList = attrService.getRelationAttr(attrGroupId);
        return R.ok().put("data", attrEntityList);
    }

    /**
     * 获取当前分组没有关联的所有属性
     */
    @GetMapping("/{attrgroupId}/noattr/relaction")
    public R attrNoRelation(@PathVariable("attrgroupId") Long attrGroupId, @RequestParam Map<String, Object> params) {
        PageUtils page = attrService.getNoRelationAttr(attrGroupId, params);
        return R.ok().put("page", page);
    }

    /**
     * 删除关联关系
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelactionVO[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long categoryId) {

        PageUtils page = attrGroupService.queryPage(params, categoryId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long cateLogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCateLogPath(cateLogId);

        attrGroup.setCatelogPath(path);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}

package cn.bugstack.mall.product.controller;

import java.util.Arrays;
import java.util.Map;

import cn.bugstack.mall.product.vo.AttrGroupRelactionVO;
import cn.bugstack.mall.product.vo.AttrRespVO;
import cn.bugstack.mall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.product.entity.AttrEntity;
import cn.bugstack.mall.product.service.AttrService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;


/**
 * 商品属性
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:47
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId, @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryBaseAttrList(params, catelogId,type);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrRespVO respVO = attrService.findAttrInfo(attrId);
        return R.ok().put("attr", respVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVO attr) {
        attrService.saveAttrDetail(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVO attr) {
        attrService.updateAttrInfo(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}

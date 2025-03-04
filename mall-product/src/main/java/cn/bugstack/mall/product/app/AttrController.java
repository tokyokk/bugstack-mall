package cn.bugstack.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.bugstack.mall.product.entity.ProductAttrValueEntity;
import cn.bugstack.mall.product.service.ProductAttrValueService;
import cn.bugstack.mall.product.vo.AttrRespVO;
import cn.bugstack.mall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 获取spu规格
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> attrValueEntityList = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", attrValueEntityList);
    }

    /**
     * 获取属性关联信息
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId, @PathVariable("attrType") String type) {
        PageUtils page = attrService.queryBaseAttrList(params, catelogId, type);
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
    public R save(@RequestBody AttrVO attr) {
        attrService.saveAttrDetail(attr);

        return R.ok();
    }

    /**
     * 修改商品规格
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> productAttrValueEntityList) {
        productAttrValueService.updateSpuAttr(spuId, productAttrValueEntityList);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVO attr) {
        attrService.updateAttrInfo(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}

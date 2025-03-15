package cn.bugstack.mall.product.app;

import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.product.entity.SkuSaleAttrValueEntity;
import cn.bugstack.mall.product.service.SkuSaleAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * sku销售属性&值
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:47
 */
@RestController
@RequestMapping("product/skusaleattrvalue")
public class SkuSaleAttrValueController {
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @GetMapping("/stringlist/{skuId}")
    public List<String> getSkuSaleAttrValues(@PathVariable("skuId") final Long skuId) {
        return skuSaleAttrValueService.getSkuSaleAttrValuesAsStringList(skuId);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:skusaleattrvalue:list")
    public R list(@RequestParam final Map<String, Object> params) {
        final PageUtils page = skuSaleAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("product:skusaleattrvalue:info")
    public R info(@PathVariable("id") final Long id) {
        final SkuSaleAttrValueEntity skuSaleAttrValue = skuSaleAttrValueService.getById(id);

        return R.ok().put("skuSaleAttrValue", skuSaleAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:skusaleattrvalue:save")
    public R save(@RequestBody final SkuSaleAttrValueEntity skuSaleAttrValue) {
        skuSaleAttrValueService.save(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:skusaleattrvalue:update")
    public R update(@RequestBody final SkuSaleAttrValueEntity skuSaleAttrValue) {
        skuSaleAttrValueService.updateById(skuSaleAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:skusaleattrvalue:delete")
    public R delete(@RequestBody final Long[] ids) {
        skuSaleAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

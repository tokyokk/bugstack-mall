package cn.bugstack.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cn.bugstack.common.valid.InsertGroup;
import cn.bugstack.common.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.bugstack.mall.product.entity.BrandEntity;
import cn.bugstack.mall.product.service.BrandService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;


/**
 * 品牌
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:47
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 查询品牌列表信息根据brandId集合
     */
    @RequestMapping("/infos")
    public R info(@RequestParam("brandIds") List<Long> brandIds) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * <p>
     * BindingResult：可以获取校验的结果
     */
    @RequestMapping("/save")
    public R save(@RequestBody @Validated({InsertGroup.class}) BrandEntity brand/*, BindingResult result*/) {
        //if (result.hasErrors()) {
        //    Map<String, String> map = new HashMap<>();
        //
        //    // 获取校验的错误结果
        //    result.getFieldErrors().forEach(item -> {
        //        // 获取到错误提示
        //        String message = item.getDefaultMessage();
        //        // 获取到错误属性的名字
        //        String field = item.getField();
        //        map.put(field, message);
        //    });
        //    return R.error(400, "提交的数据不合法").put("data", map);
        //} else {
        //    brandService.save(brand);
        //}

        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody @Validated({UpdateGroup.class}) BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}

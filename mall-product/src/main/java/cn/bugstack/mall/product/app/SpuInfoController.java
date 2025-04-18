package cn.bugstack.mall.product.app;

import java.util.Arrays;
import java.util.Map;

import cn.bugstack.mall.product.vo.SpuSaveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.product.entity.SpuInfoEntity;
import cn.bugstack.mall.product.service.SpuInfoService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;


/**
 * spu信息
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:54:48
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {

    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 根据skuId查询spu信息
     */
    @GetMapping("/{skuId}/spuinfo")
    public R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SpuInfoEntity spuInfo =  spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(spuInfo);
    }

    /**
     * 商品上架
     */
    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId) {
        spuInfoService.up(spuId);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = spuInfoService.queryPageByCondition(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SpuInfoEntity spuInfo = spuInfoService.getById(id);
        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SpuSaveVO vo) {
        spuInfoService.saveSpuInfo(vo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SpuInfoEntity spuInfo) {
        spuInfoService.updateById(spuInfo);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        spuInfoService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}

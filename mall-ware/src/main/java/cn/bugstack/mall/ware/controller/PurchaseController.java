package cn.bugstack.mall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.bugstack.mall.ware.vo.MergeVO;
import cn.bugstack.mall.ware.vo.PurchaseDoneVO;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.ware.entity.PurchaseEntity;
import cn.bugstack.mall.ware.service.PurchaseService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;



/**
 * 采购信息
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:35:20
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 完成采购单
     */
    public R done(@RequestBody PurchaseDoneVO purchaseDoneVO){
        purchaseService.done(purchaseDoneVO);
        return R.ok();
    }

    /**
     * 领取采购单
     */
    @RequestMapping("/received")
    public R receivedPurchase(@RequestBody List<Long> ids){
        purchaseService.receivedPurchase(ids);
        return R.ok();
    }

    /**
     * 合并采购单
     */
    @PostMapping("/merge")
    public R mergePurchase(@RequestBody MergeVO mergeVO){
        purchaseService.mergePurchase(mergeVO);
        return R.ok();
    }

    /**
     * 查询状态为新建与已分配的采购单
     */
    @RequestMapping("/unreceive/list")
    public R unReceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceiveList(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}

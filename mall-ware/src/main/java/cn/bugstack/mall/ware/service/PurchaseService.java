package cn.bugstack.mall.ware.service;

import cn.bugstack.mall.ware.vo.MergeVO;
import cn.bugstack.mall.ware.vo.PurchaseDoneVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:35:20
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceiveList(Map<String, Object> params);

    void mergePurchase(MergeVO mergeVO);

    void receivedPurchase(List<Long> ids);

    void done(PurchaseDoneVO purchaseDoneVO);
}


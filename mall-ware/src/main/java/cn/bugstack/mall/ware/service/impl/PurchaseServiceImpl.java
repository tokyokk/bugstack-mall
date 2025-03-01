package cn.bugstack.mall.ware.service.impl;

import cn.bugstack.common.constant.WareConstant;
import cn.bugstack.mall.ware.entity.PurchaseDetailEntity;
import cn.bugstack.mall.ware.service.PurchaseDetailService;
import cn.bugstack.mall.ware.service.WareSkuService;
import cn.bugstack.mall.ware.vo.MergeVO;
import cn.bugstack.mall.ware.vo.PurchaseDoneVO;
import cn.bugstack.mall.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.ware.dao.PurchaseDao;
import cn.bugstack.mall.ware.entity.PurchaseEntity;
import cn.bugstack.mall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceiveList(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", 0).or().eq("status", 1)
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVO mergeVO) {
        Long purchaseId = mergeVO.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            baseMapper.insert(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        List<Long> items = mergeVO.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntityList = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailsEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntityList);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        baseMapper.updateById(purchaseEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void receivedPurchase(List<Long> ids) {
        // 1.确认当前采购单是哪个状态[新建、已分配]
        List<PurchaseEntity> purchaseEntityList = ids.stream().map(id -> {
            PurchaseEntity purchaseEntity = baseMapper.selectById(id);
            return purchaseEntity;
        }).filter(purchaseEntity -> {
            if (purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode()
                    || purchaseEntity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(purchaseEntity -> {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            purchaseEntity.setUpdateTime(new Date());
            return purchaseEntity;
        }).collect(Collectors.toList());

        // 2.改变采购单状态
        this.updateBatchById(purchaseEntityList);

        // 3.修改采购单的状态
        purchaseEntityList.forEach(purchaseEntity -> {
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailService.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", purchaseEntity.getId()));
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailEntityList.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
                purchaseDetail.setId(purchaseDetailEntity.getId());
                purchaseDetail.setStatus(WareConstant.PurchaseDetailsEnum.BUYING.getCode());
                return purchaseDetail;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void done(PurchaseDoneVO purchaseDoneVO) {
        // 1. 改变采购项状态
        boolean flag = true;
        List<PurchaseDetailEntity> purchaseItemDoneVOList = new ArrayList<>();
        List<PurchaseItemDoneVO> purchaseItemDoneVo = purchaseDoneVO.getItems();
        for (PurchaseItemDoneVO item : purchaseItemDoneVo) {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.HASERROR.getCode()) {
                flag = false;
                purchaseDetail.setStatus(item.getStatus());
            } else {
                purchaseDetail.setStatus(WareConstant.PurchaseDetailsEnum.FINISH.getCode());
                // 3.将成功的采购项进行入库
                PurchaseDetailEntity purchaseDetailEntity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(purchaseDetailEntity.getSkuId(), purchaseDetailEntity.getWareId(), purchaseDetailEntity.getSkuNum());
            }
            purchaseDetail.setId(item.getItemId());
            purchaseItemDoneVOList.add(purchaseDetail);
        }
        purchaseDetailService.updateBatchById(purchaseItemDoneVOList);

        // 2. 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVO.getId());
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

}
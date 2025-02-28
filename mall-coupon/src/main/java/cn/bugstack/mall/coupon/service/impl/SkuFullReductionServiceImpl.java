package cn.bugstack.mall.coupon.service.impl;

import cn.bugstack.common.to.MemberPrice;
import cn.bugstack.common.to.SkuReductionTO;
import cn.bugstack.mall.coupon.entity.MemberPriceEntity;
import cn.bugstack.mall.coupon.entity.SkuLadderEntity;
import cn.bugstack.mall.coupon.service.MemberPriceService;
import cn.bugstack.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.coupon.dao.SkuFullReductionDao;
import cn.bugstack.mall.coupon.entity.SkuFullReductionEntity;
import cn.bugstack.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private SkuFullReductionService skuFullReductionService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTO skuReductionTO) {
        // 保存sku的优惠、满减等信息 mall_sms: sms_sku_ladder/sms_sku_full_reduction/sms_member_price
        SkuLadderEntity skuLadderEntity = SkuLadderEntity.builder()
                .skuId(skuReductionTO.getSkuId())
                .fullCount(skuReductionTO.getFullCount())
                .discount(skuReductionTO.getDiscount())
                .addOther(skuReductionTO.getCountStatus())
                .build();

        if (skuReductionTO.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTO, skuFullReductionEntity);
        if (skuReductionTO.getFullPrice().compareTo(new BigDecimal(BigInteger.ZERO)) == 1) {
            skuFullReductionService.save(skuFullReductionEntity);
        }

        List<MemberPrice> memberPriceList = skuReductionTO.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntityList = memberPriceList.stream().map(memberPrice ->
                        MemberPriceEntity.builder()
                                .skuId(skuReductionTO.getSkuId())
                                .memberLevelId(memberPrice.getId())
                                .memberLevelName(memberPrice.getName())
                                .memberPrice(memberPrice.getPrice())
                                .addOther(1)
                                .build()
                )
                .filter(item -> item.getMemberPrice().compareTo(new BigDecimal(BigInteger.ZERO)) == 1)
                .collect(Collectors.toList());

        memberPriceService.saveBatch(memberPriceEntityList);
    }

}
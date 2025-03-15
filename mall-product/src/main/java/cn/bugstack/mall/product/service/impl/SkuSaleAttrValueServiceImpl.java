package cn.bugstack.mall.product.service.impl;

import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;
import cn.bugstack.mall.product.dao.SkuSaleAttrValueDao;
import cn.bugstack.mall.product.entity.SkuSaleAttrValueEntity;
import cn.bugstack.mall.product.service.SkuSaleAttrValueService;
import cn.bugstack.mall.product.vo.SkuItemSaleAttrVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(final Map<String, Object> params) {
        final IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttrVO> getSaleAttrsBySpuId(final Long spuId) {
        return this.baseMapper.getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(final Long skuId) {
        return baseMapper.getSkuSaleAttrValuesAsStringList(skuId);
    }

}
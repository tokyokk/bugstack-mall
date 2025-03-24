package cn.bugstack.mall.ware.service.impl;

import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.ware.feign.MemberFeignService;
import cn.bugstack.mall.ware.vo.MemberAddressVO;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.ware.dao.WareInfoDao;
import cn.bugstack.mall.ware.entity.WareInfoEntity;
import cn.bugstack.mall.ware.service.WareInfoService;

@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    private final MemberFeignService memberFeignService;

    public WareInfoServiceImpl(MemberFeignService memberFeignService) {
        this.memberFeignService = memberFeignService;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");

        if (StringUtils.isNotBlank(key)) {
            queryWrapper.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key);
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addrId) {
        R r = memberFeignService.addrInfo(addrId);
        if (r.getCode() == 0) {
            MemberAddressVO data = r.getData("memberReceiveAddress",new TypeReference<MemberAddressVO>() {
            });
            if (data != null) {
                String phone = data.getPhone();
                String fare = phone.substring(phone.length() - 1, phone.length());
                return new BigDecimal(fare);
            }
        }
        return new BigDecimal("0");
    }

}
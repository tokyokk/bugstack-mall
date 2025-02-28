package cn.bugstack.mall.coupon.dao;

import cn.bugstack.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:16:29
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

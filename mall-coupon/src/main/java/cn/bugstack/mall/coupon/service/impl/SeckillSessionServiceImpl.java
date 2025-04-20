package cn.bugstack.mall.coupon.service.impl;

import cn.bugstack.mall.coupon.entity.SeckillSkuRelationEntity;
import cn.bugstack.mall.coupon.service.SeckillSkuRelationService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.coupon.dao.SeckillSessionDao;
import cn.bugstack.mall.coupon.entity.SeckillSessionEntity;
import cn.bugstack.mall.coupon.service.SeckillSessionService;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaysSession() {
        // 1.计算最近三天
        List<SeckillSessionEntity> list = this.list(Wrappers.<SeckillSessionEntity>lambdaQuery().between(SeckillSessionEntity::getStartTime, startTime(), endTime()));

        if (CollectionUtils.isNotEmpty(list)) {
            List<SeckillSessionEntity> seckillSessionEntityList = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> seckillSkuRelationEntityList = seckillSkuRelationService.list(Wrappers.<SeckillSkuRelationEntity>lambdaQuery().eq(SeckillSkuRelationEntity::getPromotionSessionId, id));
                session.setRelationSkus(seckillSkuRelationEntityList);
                return session;
            }).collect(Collectors.toList());
            return seckillSessionEntityList;
        }
        return null;
    }

    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime startTime = LocalDateTime.of(now, min);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
        LocalDateTime endTime = LocalDateTime.of(now, max);
        return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
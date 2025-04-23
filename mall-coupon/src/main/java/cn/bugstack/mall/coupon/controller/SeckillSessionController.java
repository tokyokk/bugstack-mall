package cn.bugstack.mall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import cn.bugstack.mall.coupon.entity.SeckillSessionEntity;
import cn.bugstack.mall.coupon.service.SeckillSessionService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;

/**
 * 秒杀活动场次
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:16:29
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController {

    private final SeckillSessionService seckillSessionService;

    public SeckillSessionController(SeckillSessionService seckillSessionService) {
        this.seckillSessionService = seckillSessionService;
    }

    @GetMapping("latest3DaysSession")
    public R getLatest3DaysSession() {
        List<SeckillSessionEntity> sessionEntityList = seckillSessionService.getLatest3DaysSession();
        return R.ok().setData(sessionEntityList);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSessionService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);
        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.save(seckillSession);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.updateById(seckillSession);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSessionService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}

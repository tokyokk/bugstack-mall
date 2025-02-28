package cn.bugstack.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.ware.entity.WareOrderTaskDetailEntity;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:35:20
 */
public interface WareOrderTaskDetailService extends IService<WareOrderTaskDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


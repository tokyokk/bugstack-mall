package cn.bugstack.mall.product.service;

import cn.bugstack.mall.product.vo.AttrGroupWithAttrsVO;
import cn.bugstack.mall.product.vo.SkuItemVO;
import cn.bugstack.mall.product.vo.SpuItemAttrGroupVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCateLogId(Long cateLogId);

    List<SpuItemAttrGroupVO> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}


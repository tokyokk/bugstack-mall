package cn.bugstack.mall.product.service;

import cn.bugstack.mall.product.vo.AttrGroupRelactionVO;
import cn.bugstack.mall.product.vo.AttrRespVO;
import cn.bugstack.mall.product.vo.AttrVO;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.product.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-15 22:38:22
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrDetail(AttrVO attr);

    PageUtils queryBaseAttrList(Map<String, Object> params, Long catelogId, String type);

    AttrRespVO findAttrInfo(Long attrId);

    void updateAttrInfo(AttrVO attr);

    List<AttrEntity> getRelationAttr(Long attrGroupId);

    void deleteRelation(AttrGroupRelactionVO[] vos);

    PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params);

    List<Long> selectSearchAttrIds(List<Long> attrIds);
}


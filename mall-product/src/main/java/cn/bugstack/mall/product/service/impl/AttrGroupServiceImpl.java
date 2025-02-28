package cn.bugstack.mall.product.service.impl;

import cn.bugstack.mall.product.entity.AttrEntity;
import cn.bugstack.mall.product.service.AttrService;
import cn.bugstack.mall.product.vo.AttrGroupWithAttrsVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.AttrGroupDao;
import cn.bugstack.mall.product.entity.AttrGroupEntity;
import cn.bugstack.mall.product.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {

        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> attrGroupEntityQueryWrapper = new QueryWrapper<AttrGroupEntity>();

        if (!StringUtils.isEmpty(key)) {
            attrGroupEntityQueryWrapper.and((queryWrapper) -> {
                queryWrapper.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }

        if (categoryId == 0) {
            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), attrGroupEntityQueryWrapper);
            return new PageUtils(page);
        } else {
            attrGroupEntityQueryWrapper.eq("catelog_id", categoryId);

            IPage<AttrGroupEntity> page =
                    this.page(new Query<AttrGroupEntity>().getPage(params), attrGroupEntityQueryWrapper);
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCateLogId(Long cateLogId) {
        // 1.查询当前分类下的所有属性分组
        List<AttrGroupEntity> attrGroupEntities = this.list(Wrappers.<AttrGroupEntity>lambdaQuery()
                .eq(AttrGroupEntity::getCatelogId, cateLogId));

        if (attrGroupEntities == null || attrGroupEntities.size() == 0) {
            return Collections.emptyList();
        }

        return attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVO attrGroupWithAttrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVO);

            // 2.查询当前分组下的所有属性
            List<AttrEntity> relationAttrList = attrService.getRelationAttr(attrGroupEntity.getAttrGroupId());
            attrGroupWithAttrsVO.setAttrs(relationAttrList);

            return attrGroupWithAttrsVO;
        }).collect(Collectors.toList());
    }

}
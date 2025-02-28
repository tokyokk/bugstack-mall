package cn.bugstack.mall.product.service.impl;

import cn.bugstack.common.constant.ProductConstant;
import cn.bugstack.mall.product.dao.AttrAttrgroupRelationDao;
import cn.bugstack.mall.product.dao.AttrGroupDao;
import cn.bugstack.mall.product.dao.CategoryDao;
import cn.bugstack.mall.product.entity.AttrAttrgroupRelationEntity;
import cn.bugstack.mall.product.entity.AttrGroupEntity;
import cn.bugstack.mall.product.entity.CategoryEntity;
import cn.bugstack.mall.product.service.AttrAttrgroupRelationService;
import cn.bugstack.mall.product.service.CategoryService;
import cn.bugstack.mall.product.vo.AttrGroupRelactionVO;
import cn.bugstack.mall.product.vo.AttrRespVO;
import cn.bugstack.mall.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.Query;

import cn.bugstack.mall.product.dao.AttrDao;
import cn.bugstack.mall.product.entity.AttrEntity;
import cn.bugstack.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAttrDetail(AttrVO attr) {
        AttrEntity attrEntity = AttrEntity.builder()
                .attrId(attr.getAttrId())
                .attrName(attr.getAttrName())
                .searchType(attr.getSearchType())
                .icon(attr.getIcon())
                .valueSelect(attr.getValueSelect())
                .attrType(attr.getAttrType())
                .enable(attr.getEnable())
                .catelogId(attr.getCatelogId())
                .showDesc(attr.getShowDesc())
                .build();
        // 1. 保存基本数据
        this.save(attrEntity);
        // 2. 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());

            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryBaseAttrList(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper =
                new QueryWrapper<AttrEntity>()
                        .eq("attr_type", "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), queryWrapper
        );

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> attrEntityList = page.getRecords();

        List<AttrRespVO> respList = attrEntityList.stream().map((attrEntity -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRespVO);

            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService.getOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId())
            );

            if ("base".equalsIgnoreCase(type)) {
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    if (attrGroupEntity != null) {
                        attrRespVO.setAttrName(attrGroupEntity.getAttrGroupName());
                    }
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            Optional.ofNullable(categoryEntity).ifPresent(item -> {
                attrRespVO.setCatelogName(item.getName());
            });


            return attrRespVO;
        })).collect(Collectors.toList());

        pageUtils.setList(respList);

        return pageUtils;
    }

    @Override
    public AttrRespVO findAttrInfo(Long attrId) {
        AttrRespVO attrRespVO = new AttrRespVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVO);
        AttrAttrgroupRelationEntity relationEntity =
                attrAttrgroupRelationService.getOne(Wrappers.<AttrAttrgroupRelationEntity>lambdaQuery().eq(AttrAttrgroupRelationEntity::getAttrId, attrId));

        // 1. 设置分组信息
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            Optional.ofNullable(relationEntity).ifPresent(attrRelation -> {
                attrRespVO.setAttrGroupId(attrRelation.getAttrGroupId());
                AttrGroupEntity attrGroupEntity =
                        attrGroupDao.selectOne(Wrappers.<AttrGroupEntity>lambdaQuery().eq(AttrGroupEntity::getAttrGroupId, relationEntity.getAttrGroupId()));
                Optional.ofNullable(attrGroupEntity).ifPresent(attrGroup -> {
                    attrRespVO.setGroupName(attrGroup.getAttrGroupName());
                });

            });
        }

        // 2. 设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] cateLogPath = categoryService.findCateLogPath(catelogId);
        attrRespVO.setCatelogPath(cateLogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        Optional.ofNullable(categoryEntity).ifPresent(category -> {
            attrRespVO.setCatelogName(category.getName());
        });
        return attrRespVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttrInfo(AttrVO attr) {
        AttrEntity attrEntity = AttrEntity.builder()
                .attrId(attr.getAttrId())
                .attrName(attr.getAttrName())
                .searchType(attr.getSearchType())
                .icon(attr.getIcon())
                .valueSelect(attr.getValueSelect())
                .attrType(attr.getAttrType())
                .enable(attr.getEnable())
                .catelogId(attr.getCatelogId())
                .showDesc(attr.getShowDesc())
                .build();
        this.updateById(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));

            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());

            if (count > 0) {
                // 1. 修改分组关联
                attrAttrgroupRelationService.update(attrAttrgroupRelationEntity,
                        new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId())
                );
            } else {
                attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntityList =
                attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_group_id", attrGroupId));

        List<Long> attrIds = relationEntityList.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId)
                .collect(Collectors.toList());

        if (attrIds.isEmpty() || attrIds == null) {
            return null;
        }

        return (List<AttrEntity>) this.listByIds(attrIds);
    }

    @Override
    public void deleteRelation(AttrGroupRelactionVO[] vos) {
        List<AttrAttrgroupRelationEntity> relationEntityList =
                Arrays.stream(vos).map(item -> {
                    AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
                    return attrAttrgroupRelationEntity;
                }).collect(Collectors.toList());

        this.baseMapper.deleteBatchRelation(relationEntityList);
    }

    @Override
    public PageUtils getNoRelationAttr(Long attrGroupId, Map<String, Object> params) {
        // 1，当前分组只能关联自己所属分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
        Long catelogId = attrGroupEntity.getCatelogId();

        // 2。当前分组只能关联别的分组没有引用的属性
        // 2.1 查询当前分类下的分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIds = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());

        // 2.2 查询这些分组关联的属性
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities =
                attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIds));

        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        // 2.3 从当前分类的所有属性中移除这些属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        if (attrIds != null && !attrIds.isEmpty()) {
            queryWrapper.notIn("attr_id", attrIds);
        }

        String key = (String) params.get("key");
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(item -> {
                item.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

}
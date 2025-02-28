package cn.bugstack.mall.member.dao;

import cn.bugstack.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:25:58
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}

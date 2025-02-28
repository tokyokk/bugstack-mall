package cn.bugstack.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:25:58
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


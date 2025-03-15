package cn.bugstack.mall.member.service;

import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.mall.member.entity.MemberEntity;
import cn.bugstack.mall.member.exception.PhoneExistException;
import cn.bugstack.mall.member.exception.UserNameExistException;
import cn.bugstack.mall.member.vo.MemberLoginVO;
import cn.bugstack.mall.member.vo.MemberRegisterVO;
import cn.bugstack.mall.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;

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

    void register(MemberRegisterVO memberRegister);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UserNameExistException;

    MemberEntity login(MemberLoginVO memberLoginVO);

    MemberEntity login(SocialUser socialUser) throws Exception;
}


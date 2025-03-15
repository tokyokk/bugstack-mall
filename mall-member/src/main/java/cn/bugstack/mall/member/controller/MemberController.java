package cn.bugstack.mall.member.controller;

import cn.bugstack.common.exception.BizCodeEnum;
import cn.bugstack.common.utils.PageUtils;
import cn.bugstack.common.utils.R;
import cn.bugstack.mall.member.entity.MemberEntity;
import cn.bugstack.mall.member.exception.PhoneExistException;
import cn.bugstack.mall.member.exception.UserNameExistException;
import cn.bugstack.mall.member.service.MemberService;
import cn.bugstack.mall.member.vo.MemberLoginVO;
import cn.bugstack.mall.member.vo.MemberRegisterVO;
import cn.bugstack.mall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author micro
 * @email z175828511840@163.com
 * @date 2025-02-17 20:25:58
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 注册
     */
    @PostMapping("/register")
    public R register(@RequestBody final MemberRegisterVO memberRegister) {
        try {
            memberService.register(memberRegister);
        } catch (final PhoneExistException phoneExistException) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (final UserNameExistException userNameExistException) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 社交登录
     */
    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody final SocialUser socialUser) throws Exception {
        final MemberEntity memberEntity = memberService.login(socialUser);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody final MemberLoginVO memberLoginVO) {
        final MemberEntity memberEntity = memberService.login(memberLoginVO);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam final Map<String, Object> params) {
        final PageUtils page = memberService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") final Long id) {
        final MemberEntity member = memberService.getById(id);
        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody final MemberEntity member) {
        memberService.save(member);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody final MemberEntity member) {
        memberService.updateById(member);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody final Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}

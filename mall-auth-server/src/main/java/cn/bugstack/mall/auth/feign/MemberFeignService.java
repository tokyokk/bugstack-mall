package cn.bugstack.mall.auth.feign;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.auth.vo.SocialUser;
import cn.bugstack.mall.auth.vo.UserLoginVO;
import cn.bugstack.mall.auth.vo.UserRegisterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/14 00:37
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    public R register(@RequestBody UserRegisterVO userRegisterVO);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVO userLoginVO);

    @PostMapping("/member/member/oauth2/login")
    public R oauthLogin(@RequestBody final SocialUser socialUser) throws Exception;
}

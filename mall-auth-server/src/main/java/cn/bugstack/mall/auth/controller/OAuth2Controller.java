package cn.bugstack.mall.auth.controller;

import cn.bugstack.common.utils.HttpUtils;
import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.auth.feign.MemberFeignService;
import cn.bugstack.mall.auth.vo.SocialUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * @author micro, 微信：yykk、
 * @description 处理社交登录请求
 * @date 2025/3/15 00:41
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Slf4j
@Controller
public class OAuth2Controller {

    private final MemberFeignService memberFeignService;

    public OAuth2Controller(final MemberFeignService memberFeignService) {
        this.memberFeignService = memberFeignService;
    }

    /**
     * 社交登录成功回调
     *
     * @param code code
     * @return {@link String }
     * @throws Exception 异常
     */
    @GetMapping("oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") final String code, final HttpSession session, final HttpServletResponse servletResponse) throws Exception {

        final HashMap<String, String> map = Maps.newHashMap();
        map.put("client_id", "");
        map.put("client_secret", "");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.mall.com/oauth2.0/weibo/success");
        map.put("code", code);
        // 1.根据code换取令牌accessToken
        final HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "POST", null, null, map);
        if (response.getStatusLine().getStatusCode() == 200) {
            // 2.成功
            // 3.处理
            final String json = EntityUtils.toString(response.getEntity());
            final SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            log.info("登录成功，用户信息：{}", socialUser);
            // 登录成功之后就知道是哪个社交用户了
            // 1.如果用户第一次使用社交账号登录，则自动注册进来（为当前社交账号生成一个会员信息，以后这个社交账号就对应指定的会员）
            final R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0) {
                final MemberResponseVO data = r.getData("data", new TypeReference<MemberResponseVO>() {
                });
                // 第一次使用session，命令浏览器保存JSessionId，每次访问带上cookie
                // 子域之间共享：mall.com order.mall.com auth.mall.com
                // 发送cookie，放大作用域，指定为mall.com 为父域
                //new Cookie("auth", "").setDomain("mall.com");
                //servletResponse.addCookie();
                // todo：1.默认发的令牌。SESSION=dajkjkdajsk。作用域：当前域：（解决子域session共享问题）
                // todo：2.使用json序列化的方式将对象存储到redis
                session.setAttribute("loginUser", data);
                log.info("登录成功，用户信息：{}", data.toString());
                // 2.根据accessToken换取用户信息，登录成功跳回首页
                return "redirect:http://mall.com";

            } else {
                return "redirect:http://mall.com";
            }
        } else {
            // 2.失败
            return "redirect:http://auth.mall.com/login.html";
        }

    }
}

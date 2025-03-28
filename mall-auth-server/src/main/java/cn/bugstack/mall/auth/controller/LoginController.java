package cn.bugstack.mall.auth.controller;

import cn.bugstack.common.constant.AuthServerConstant;
import cn.bugstack.common.exception.BizCodeEnum;
import cn.bugstack.common.utils.R;
import cn.bugstack.common.vo.MemberResponseVO;
import cn.bugstack.mall.auth.feign.MemberFeignService;
import cn.bugstack.mall.auth.feign.ThirdPartyFeignService;
import cn.bugstack.mall.auth.vo.UserLoginVO;
import cn.bugstack.mall.auth.vo.UserRegisterVO;
import com.alibaba.fastjson.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/8 19:47
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@Controller
public class LoginController {


    private final ThirdPartyFeignService thirdPartyFeignService;
    private final StringRedisTemplate redisTemplate;
    private final MemberFeignService memberFeignService;

    public LoginController(final ThirdPartyFeignService thirdPartyFeignService,
                           final StringRedisTemplate redisTemplate,
                           final MemberFeignService memberFeignService) {
        this.thirdPartyFeignService = thirdPartyFeignService;
        this.redisTemplate = redisTemplate;
        this.memberFeignService = memberFeignService;
    }

    @GetMapping("/login.html")
    public String loginPage(final HttpSession session) {
        final Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (null != attribute) {
            return "redirect:http://mall.com";
        }
        return "login";
    }


    /**
     * 发送验证码
     *
     * @param phone 电话
     * @return {@link R }
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") final String phone) {
        // todo：1.接口防刷
        final String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            final long redisCodeTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - redisCodeTime < 60000) {
                // 60秒内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 2. 验证码的再次校验：redis
        final String substring = UUID.randomUUID().toString().substring(0, 5);
        final String code = substring + "_" + System.currentTimeMillis();
        thirdPartyFeignService.sendCode(phone, code);
        // redis缓存验证码，防止手机号在60秒内重复发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, substring, 10, TimeUnit.MINUTES);
        return R.ok();
    }

    /**
     * 注册
     *
     * @param userRegisterVO     用户注册vo
     * @param result             验证结果
     * @param redirectAttributes 模拟重定向携带数据 todo：重定向携带数据，利用session原理，将数据放在session中，只要跳到下一个页面取出这个数据之后，session里面的数据就会删掉
     * @return {@link String }
     */
    @PostMapping("register")
    public String register(@Valid final UserRegisterVO userRegisterVO, final BindingResult result, final RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {

            final Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(
                    FieldError::getField, FieldError::getDefaultMessage
            ));
            /*model.addAttribute("errors", errors);*/
            redirectAttributes.addFlashAttribute("errors", errors);
            // Request method 'POST' not supported
            // 用户注册：register[post] --> 转发/reg.html (路径映射默认都是get方式访问的)

            // 校验出错，返回注册页
            //return "forward:/reg.html"; // 所以不使用这个方式
            return "redirect:http://auth.mall.com/reg.html";
        }

        // 1.校验验证码
        final String code = userRegisterVO.getCode();
        final String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVO.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (redisCode.split("_")[0].equals(code)) {
                // 删除验证码：令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisterVO.getPhone());
                // 验证码通过，调用远程注册服务
                final R r = memberFeignService.register(userRegisterVO);
                if (r.getCode() == 0) {
                    // 成功
                    return "redirect:http://auth.mall.com/login.html";
                } else {
                    // 失败
                    final Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mall.com/reg.html";
                }

            } else {
                redirectAttributes.addFlashAttribute("errors", new HashMap<String, String>(1).put("code", "验证码错误"));
                return "redirect:http://auth.mall.com/reg.html";
            }
        } else {
            redirectAttributes.addFlashAttribute("errors", new HashMap<String, String>(1).put("code", "验证码错误"));
            return "redirect:http://auth.mall.com/reg.html";
        }

    }

    @PostMapping("/login")
    public String login(final UserLoginVO userLoginVO, final RedirectAttributes redirectAttributes, final HttpSession session) {
        final R r = memberFeignService.login(userLoginVO);
        if (r.getCode() == 0) {
            // 成功
            // 1.将登录成功的信息放在session中
            final MemberResponseVO data = r.getData("data", new TypeReference<MemberResponseVO>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, data);
            // 2.跳转到首页
            return "redirect:http://mall.com";
        } else {
            final Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mall.com/login.html";
        }
        //return "redirect:http://mall.com";
    }



    /*
        发送一个请求直接跳转到页面
        SpringMVC ViewController：将请求和页面映射过来
    */

    /*@GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/reg.html")
    public String regPage() {
        return "reg";
    }*/
}

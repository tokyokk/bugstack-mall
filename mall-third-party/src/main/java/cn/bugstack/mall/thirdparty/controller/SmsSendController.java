package cn.bugstack.mall.thirdparty.controller;

import cn.bugstack.common.utils.R;
import cn.bugstack.mall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/13 21:41
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.top - 沉淀、分享、成长。让自己和他人都有所收获！
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    private SmsComponent smsComponent;


    /**
     * 提供给别的服务进行调用，发送验证码
     *
     * @param phone 手机号
     * @param code  code
     * @return 验证码信息
     */
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code) {
        smsComponent.sendCode(phone, code);
        return R.ok();
    }
}

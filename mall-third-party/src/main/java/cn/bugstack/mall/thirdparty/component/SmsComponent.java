package cn.bugstack.mall.thirdparty.component;

import cn.bugstack.mall.thirdparty.utils.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/******
 @author 阿昌
 @create 2021-10-17 16:50
 *******
 */
@Component
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
public class SmsComponent {

    private String host;
    private String path;
    private String method;
    private String appcode;
    private String channel;
    private String templateID;



    public void sendCode(String phone,String code){
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("callbackUrl", "http://test.dev.esandcloud.com");
        bodys.put("channel", channel);
        bodys.put("mobile", "+86"+phone);
        bodys.put("templateID", templateID);
        bodys.put("templateParamSet", code+", 1");


        try {
            HttpResponse  response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}

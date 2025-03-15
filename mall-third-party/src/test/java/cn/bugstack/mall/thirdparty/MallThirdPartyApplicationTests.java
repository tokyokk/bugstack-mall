package cn.bugstack.mall.thirdparty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallThirdPartyApplicationTests {

    @Test
    void contextLoads() {
    }

    //@Autowired
    //private OSSClient ossClient;

    //@Test
    //void ossTest() throws FileNotFoundException {
    //    FileInputStream fileInputStream = new FileInputStream("");
    //    ossClient.putObject("mall-product", "a.jpg", fileInputStream);
    //    ossClient.shutdown();
    //    System.out.println("上传成功");
    //}


    //@Autowired
    //private SmsComponent smsComponent;


    //@Test
    //public void test(){
    //    smsComponent.sendCode("18758720408","6379");
    //}

//    public static void main(String[] args) {
//        String host = "https://intlsms.market.alicloudapi.com";
//        String path = "/comms/sms/sendmsgall";
//        String method = "POST";
//        String appcode = "03e50531482048499fd8909854703a5b";
//        Map<String, String> headers = new HashMap<String, String>();
//        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);
//        //根据API的要求，定义相对应的Content-Type
//        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//        Map<String, String> querys = new HashMap<String, String>();
//        Map<String, String> bodys = new HashMap<String, String>();
//        bodys.put("callbackUrl", "http://test.dev.esandcloud.com");
//        bodys.put("channel", "0");
//        bodys.put("mobile", "+8618757750375");
//        bodys.put("templateID", "0000000");
//        bodys.put("templateParamSet", "1234, 1");
//
//
//        try {
//            /**
//             * 重要提示如下:
//             * HttpUtils请从
//             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
//             * 下载
//             *
//             * 相应的依赖请参照
//             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
//             */
//            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(response.toString());
//            //获取response的body
//            //System.out.println(EntityUtils.toString(response.getEntity()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}

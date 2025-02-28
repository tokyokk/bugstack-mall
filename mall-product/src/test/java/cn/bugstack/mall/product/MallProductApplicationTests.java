package cn.bugstack.mall.product;

import cn.bugstack.mall.product.entity.BrandEntity;
import cn.bugstack.mall.product.service.BrandService;
import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private OSSClient ossClient;

    @Test
    void ossTest() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("");
        ossClient.putObject("mall-product", "a.jpg", fileInputStream);
        ossClient.shutdown();
        System.out.println("上传成功");
    }

    @Test
    void contextLoads() {

        //BrandEntity brandEntity = new BrandEntity();
        //brandEntity.setName("华为");
        //
        //brandService.save(brandEntity);
        //System.out.println("保存成功");

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("华为手机");
        brandService.updateById(brandEntity);
    }

}

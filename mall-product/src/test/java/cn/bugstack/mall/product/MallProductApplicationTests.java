package cn.bugstack.mall.product;

import cn.bugstack.mall.product.entity.BrandEntity;
import cn.bugstack.mall.product.service.BrandService;
//import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MallProductApplicationTests {

    @Autowired
    private BrandService brandService;



    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 整合步骤：
     * 1.配置文件 spring.cache.type=redis
     * 2.开启缓存注解 @EnableCaching
     * 3.注解：
     *      @Cacheable：触发将数据保存到缓存的操作
     *      @CacheEvict：触发将数据从缓存删除的操作
     *      @CachePut：不影响方法执行更新缓存
     *      @Caching：同时对多个属性进行缓存操作
     *      @CacheConfig：指定缓存的共享配置，类级别
     */
    void testSpringCache() {

    }

    @Test
    void redissonTest() {
        RLock lock = redissonClient.getLock("my-lock");
        lock.lock(10, TimeUnit.SECONDS); // 注意点：如果指定了过期时间，那么watchdog机制不会自动续签，前提：锁的时间必须大于业务执行时间
        // 1.如果我们指定了锁的超时时间，就发送给redis执行脚本，进行占用锁，超时时间就是我们指定的时间
        // 2.如果没有指定锁的超时时间，就使用30 * 10000【LockWatchdogTimeout看门狗的默认时间】
        //    只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，就是看门狗的默认时间】，每隔10s就会重置锁的过期时间，所以，只要业务执行完成，就会自动续期，不会出现业务执行时间大于锁过期时间的情况
        //    internalLockLeaseTime【看门狗时间】 / 3 = 10s
        // 最佳实战：
        //     ✅使用lock.lock(10, TimeUnit.SECONDS);省掉了续期操作，手动解锁
        System.out.println(redissonClient);
    }

    @Test
    void redisTest() {
        System.out.println(stringRedisTemplate);
        stringRedisTemplate.opsForValue().set("hello", "world_" + System.currentTimeMillis());
        System.out.println(stringRedisTemplate.opsForValue().get("hello"));
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

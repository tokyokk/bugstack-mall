package cn.bugstack.mall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 定时任务：
 *      1、开启定时任务 @EnableScheduling
 *      2、@Scheduled(cron = “”) 开启一个定时任务
 *      3、自动配置类：TaskSchedulingAutoConfiguration
 * 异步任务：
 *      1、@EnableAsync：开启异步任务
 *      2、@Async：给希望异步执行的方法上标注
 *      3、自动配置类：TaskExecutionAutoConfiguration,属性绑定在TaskExecutionProperties
 */
@Slf4j
@Component
// @EnableAsync
// @EnableScheduling
public class HelloScheduled {

    /**
     * 1、Spring中6位组成，不允许第7位年
     * 2、在周几的位置，1-7代表周一到周日：MON-SUN
     * 3、定时任务不应该阻塞。默认是阻塞的
     *      1）、可以让线程以异步的方式运行，提交到线程池
     *          CompletableFuture.runAsync(()->{
     *             service.doSomething();
     *         },executor);
     *      2）、定时任务线程池，设置：TaskSchedulingAutoConfiguration
     *          spring.task.scheduling.pool.size=10
     *      3）、异步执行定时任务
     *
     *      解决：使用定时任务+异步任务不阻塞的功能。
     */
    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello() {
        log.info("hello。。。");

    }
}

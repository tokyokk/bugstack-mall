package cn.bugstack.mall.product;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author micro, 微信：yykk、
 * @description
 * @date 2025/3/6 10:29
 * @github https://github.com/tokyokk
 * @copyright 博客：http://bugstack.cc - 沉淀、分享、成长。让自己和他人都有所收获！
 */
public class ThreadPoolCore {

    /*
        七大参数：
            1. corePoolSize核心线程数：核心线程数（一直存在，除非设置了allowCoreThreadTimeOut），线程池创建好后就准备就绪，就绪后的核心线程数，就一直存在线程池中，当有新的任务来时，就会复用核心线程，执行新的任务。
            2. maximumPoolSize：最大线程数：当核心线程都被占用，且新任务来时，就会创建非核心线程，非核心线程 also known as "worker" threads.
            3. keepAliveTime：线程空闲时间：非核心线程的存活时间，非核心线程的活跃时间。当线程超过核心线程数时，会额外创建非核心线程，执行完任务，等待时间结束后，非核心线程销毁。
                释放空闲的线程（maximumPoolSize-corePoolSize核心线程数）  。
            4. unit：时间单位：keepAliveTime参数的单位。
            5. workQueue：阻塞队列：任务队列，用于存放等待状态的线程。
            6. threadFactory：线程工厂：创建线程的工厂。用于创建线程，一般使用默认即可。
            7. handler：拒绝策略：当队列和线程池都满了，再有新任务进来时，会采取一种拒绝策略。默认情况下，当队列和线程池都满了，会抛出RejectedExecutionException异常。
                - AbortPolicy：直接抛出异常。
                - CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行。相当于同步执行了
                - DiscardPolicy：直接丢弃。
                - DiscardOldestPolicy：丢弃阻塞队列中最老的任务，把新的任务加入队列。
        工作顺序：
            1. 线程池创建，准备好core数量的核心线程，准备接受任务
            2. corePoolSize满了，就将再进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
            3. 阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
            4. max指定了线程数量，也满了，就采用拒绝策略。
            5. maximumPoolSize都执行完成，有很多的空闲线程，空闲时间超过keepAliveTime，就会释放，最大空闲线程数=maximumPoolSize-corePoolSize
                new LinkedBlockingQueue<>() ：默认是Integer.MAX_VALUE，即无限大。尽量在压测之后给上限值，避免内存溢出。例如：new LinkedBlockingQueue<>(10000)
            例题：
                一个线程池：core=7，max=20，workQueue=50，100并发访问怎么分配
                7个立即执行，50个放入阻塞队列，再开13个线程进行执行，剩下的30个就使用拒绝策略。
                如果不想抛弃还要执行，就使用CallerRunsPolicy策略。
     */
    public static void main(String[] args) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        Executors.newCachedThreadPool();  // core是0，所有都可以回收
        Executors.newFixedThreadPool(10); // core=max，不会回收
        Executors.newSingleThreadExecutor(); // 定时任务的线程池
        Executors.newSingleThreadExecutor(); // 单线程的线程池，后台从队列里面获取任务挨个执行。core=1，max=1，workQueue=Integer.MAX_VALUE

    }
}

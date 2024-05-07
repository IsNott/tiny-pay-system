package org.nott.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author Nott
 * @date 2024-5-7
 */
@Slf4j
@Configuration
public class ThreadPoolContext {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int i = Runtime.getRuntime().availableProcessors();
        //核心线程数目
        executor.setCorePoolSize(i * 2);
        //指定最大线程数
        executor.setMaxPoolSize(i * 2);
        //队列中最大的数目
        executor.setQueueCapacity(i * 2 * 10);
        //线程名称前缀
        executor.setThreadNamePrefix("TinyPay-ThreadPool-");
        //rejection-policy：当pool已经达到max size的时候，如何处理新任务
        //CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        //对拒绝task的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //当调度器shutdown被调用时等待当前被调度的任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        //线程空闲后的最大存活时间
        executor.setKeepAliveSeconds(60);
        //加载
        executor.initialize();

        return executor;
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        //获取cpu核心数
        int i = Runtime.getRuntime().availableProcessors();
        //核心线程数
        int corePoolSize = i * 2;
        //最大线程数
        int maximumPoolSize = i * 2;
        //线程无引用存活时间
        long keepAliveTime = 60;
        //时间单位
        TimeUnit unit = TimeUnit.SECONDS;
        //任务队列，接收一个整型的参数，这个整型参数指的是队列的长度，
        //ArrayBlockingQueue(int,boolean)，boolean类型的参数是作为可重入锁的参数进行初始化，默认false，另外初始化了notEmpty、notFull两个信号量。
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue(i * 2 * 10);
        //1. 同步阻塞队列 (put,take)，直接提交。直接提交策略表示线程池不对任务进行缓存。新进任务直接提交给线程池，当线程池中没有空闲线程时，创建一个新的线程处理此任务。
        // 这种策略需要线程池具有无限增长的可能性。实现为：SynchronousQueue
        //2. 有界队列。当线程池中线程达到corePoolSize时，新进任务被放在队列里排队等待处理。有界队列（如ArrayBlockingQueue）有助于防止资源耗尽，
        // 但是可能较难调整和控制。队列大小和最大池大小可能需要相互折衷：使用大型队列和小型池可以最大限度地降低 CPU 使用率、操作系统资源和上下文切换开销，
        // 但是可能导致人工降低吞吐量。如果任务频繁阻塞（例如，如果它们是 I/O 边界），则系统可能为超过您许可的更多线程安排时间。使用小型队列通常要求较大的池大小，
        // CPU 使用率较高，但是可能遇到不可接受的调度开销，这样也会降低吞吐量。
        //3. 无界队列。使用无界队列（例如，不具有预定义容量的 LinkedBlockingQueue）将导致在所有 corePoolSize 线程都忙时新任务在队列中等待。
        // 这样，创建的线程就不会超过 corePoolSize。（因此，maximumPoolSize 的值也就无效了。）当每个任务完全独立于其他任务，即任务执行互不影响时，
        // 适合于使用无界队列；例如，在 Web 页服务器中。这种排队可用于处理瞬态突发请求，当命令以超过队列所能处理的平均数连续到达时，此策略允许无界线程具有增长的可能性。

        //线程工厂
        //defaultThreadFactory()
        //返回用于创建新线程的默认线程工厂。
        //privilegedThreadFactory()
        //返回一个用于创建与当前线程具有相同权限的新线程的线程工厂。
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        //拒绝执行处理器
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        //创建线程池
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        return threadPoolExecutor;
    }
}

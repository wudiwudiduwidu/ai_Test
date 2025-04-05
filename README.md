# 趣云ai答题平台
## 项目介绍
基于 Vue3+Spring Boot + MybatisPlus + Redis + ChatGLM（智谱AI 大模型）+ RxJava + SSE 的 AI答题应用平台。用户可基于 AI 快速生成题目并制作应用，经管理员审核后，可在线答题并基于评分算法或 AI 得到回答总结;管理员还可集中管理整站内容，并进行统计分析。

### 项目运行截图
1. 首页

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blogc952ed.png){{{width="auto" height="auto"}}}

2. 创建应用

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/bloga09abd.png){{{width="auto" height="auto"}}}

3. 查看用户回答情况

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blog2b2d18.png){{{width="auto" height="auto"}}}

4. 统计分析

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blogaa31a6.png){{{width="auto" height="auto"}}}

5. 应用管理

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blog0cc07c.png){{{width="auto" height="auto"}}}

6. 答题界面

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blog74a3ce.png){{{width="auto" height="auto"}}}

### 项目本地部署
1. 使用idea连接MySQL，然后执行后端src包下的SQL
2. 修改MySQL账号密码
``` java
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mbti
    username: xzz
    password: Mdjfb123!
```
3. 在配置文件中填写智谱AI的Key 免费申请地址 [apiKey](https://www.bigmodel.cn/usercenter/proj-mgmt/apikeys)
```java
ai:
  key: "49510645eaa841b28781213f6b1110c5.8fbot56G3IWd4ypr"
```
4. 下载[redis的windows的客户端](https://github.com/tporadowski/redis/releases ) 运行如下
   ![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blogd75514.png){{{width="auto" height="auto"}}}
5. 启动后端项目
6. 打开前端（aiTest_front）
7. 命令行执行`npm install --save-dev @arco-design/web-vue`下载组件库
8. 命令行执行`npm i echarts下载`统计图插件
9. **如果前端还需提示下载，根据提示下载即可**
10. 找到 package.json文件 执行启动项目
 ``` bash
   "serve": "vue-cli-service serve"
 ```
![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blog37c501.png){{{width="auto" height="auto"}}}
7. 图片上传使用的是阿里云对象储存oss，需要去阿里云官网获得以下信息(自行查阅资料) 位于com.xz.aiTest.utils.OSSUtils中

![Description](https://xzai-platform.oss-cn-guangzhou.aliyuncs.com/blogcca8e6.png)



**如果你了解过docker，redis也可以用docker部署到服务器上**

## 项目亮点分析
### Redission 分布式锁避免缓存穿透
这里是因为在ai评分模块时，让相同应用的相同答案得到相同答案，并且存在缓存时，不调用ai。缓存未命中时，先加上Redission封装的分布式锁，确保只有一个用户更新缓存，其他用户为超时等待，后续继续走缓存
**1）代码分析**

引入Redisson-config包中

下面代码在`com.xz.aiTest.scoring.AITestScorningStrategy`类中
``` java
  RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);

        try {
            //等待3s 20s自动释放
            boolean res = lock.tryLock(3,20, TimeUnit.SECONDS);
            if (res) {
//业务
               /**
							 							 
							 
							 业务...............
							 
							 			 
							 
							 **/

                answerCacheMap.put(cacheKey, JSONUtil.toJsonStr(userAnswer));
                return userAnswer;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // lock.isHeldByCurrentThread() 确保锁是当前线程上的。
            if(lock !=null && lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }
```
**使用分布式锁需要注意以下**
1. 需要合理设置超时释放时间，避免造成死锁。
2. 业务处理完毕后及时释放锁，防止业务阻塞。
3. 必须由当前线程释放锁，不能释放其他线程加的锁，防止业务执行受到影响。
4. 合理设置抢锁等待时间，避免长时间无效等待。
5. 可以利用看门狗机制实现锁的续期，防止由于业务处理时间大于锁超时释放时间，导致一把锁
被多个线程拥有，从而出现错误。

### RxJava
RxJava可以处理流信息(flowable对象)，本项目中在ai生成题目时配合sse实现一道一道向前端发送题目。
在`com/xz/aiTest/service/impl/AiGenerateImpl.java`中可以浏览flowable是怎么处理的。
**它通常有如下格式**
``` java
//线程池 
 flowable.observeOn()
                .map(data -> data.getChoices().get(0).getDelta().getContent())
								.filter()过滤
                // 分流
                .flatMap()
                .doOnNext()
                .doOnError(e -> log.error()
                .doOnComplete()
                .subscribe();
```
具有特点：响应式编程、丰富的操作符（类stream流）。
#### RxJava的线程池隔离
在observeOn()中，我们可以传入自定义的线程，以免过多的请求导致服务器奔溃。（因为是异步的，所以默认情况下来一个请求创建一个线程）
阅读`com/xz/aiTest/config/ScheduleConfig.java`
```java
package com.xz.aiTest.config;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
@Configuration
public class ScheduleConfig {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    // 自定义线程
    @Bean
    public Scheduler vipScheduler() {

        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(r,"vip-scheduler-"+threadNumber.getAndAdd(1));
                //非守护线程
                t.setDaemon(false);
                return t;
            }
        };
        //自定义线程池
        ExecutorService scheduledExecutorService = new ThreadPoolExecutor(5, 7, 5, TimeUnit.SECONDS,new LinkedBlockingQueue<>(100),threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        return Schedulers.from(scheduledExecutorService);

    }
}

```
**为什么需要线程池隔离呢？**
1. 防止局部影响全局。
2. 不同业务配置不同的线程资源。同时支持更精细化地管理资源，比如不重要的场景给小一点的线程池，核心场景配置大线程池。
3. 一些业务场景的任务是 CPU 密集型，一些是 /0 密集型，不同任务类型需要配置不同。










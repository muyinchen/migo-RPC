##此项目的开发细节 请参考以下博文:

[一个轻量级分布式 RPC 框架 上](https://muyinchen.github.io/2017/03/16/%E8%BD%BB%E9%87%8F%E7%BA%A7%E5%88%86%E5%B8%83%E5%BC%8F%20RPC%20%E6%A1%86%E6%9E%B6%20%E4%B8%8A/)

[一个轻量级分布式 RPC 框架 下](https://muyinchen.github.io/2017/03/27/%E4%B8%80%E4%B8%AA%E8%BD%BB%E9%87%8F%E7%BA%A7%E5%88%86%E5%B8%83%E5%BC%8F%20RPC%20%E6%A1%86%E6%9E%B6%20%E4%B8%8B/)


## 示例使用

#### 这里我们通过一个`SpringbootDemo`来演示如何使用:

具体代码结构请看源码

##### 定义一个测试service接口:

```java
package com.nia.rpc.example.service;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
public interface HelloWorld {
    String say(String hello);

    int sum(int a, int b);
    int max(Integer a, Integer b);
}

```

##### 编写其实现类:

```java
package com.nia.rpc.example.service;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
public class HelloWorldImpl implements HelloWorld {
    @Override
    public String say(String hello) {
        return "server: "+hello;
    }

    @Override
    public int sum(int a, int b) {
        return a+b;
    }

    @Override
    public int max(Integer a, Integer b) {
        return a <= b ? b : a;
    }
}

```



##### 编写`Springboot`服务端启动类:

###### `SpringServerConfig`

```java
package com.nia.rpc.example.server;

import com.nia.rpc.core.utils.NetUtils;
import com.nia.rpc.example.service.HelloWorld;
import com.nia.rpc.example.service.HelloWorldImpl;
import com.nia.rpc.factory.ServerFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
@SpringBootApplication
public class SpringServerConfig {
    @Bean
    public HelloWorld hello() {
        return new HelloWorldImpl();
    }

    @Bean
    public ServerFactoryBean serverFactoryBean() {
        final ServerFactoryBean serverFactoryBean = new ServerFactoryBean();
        serverFactoryBean.setPort(9090);
        serverFactoryBean.setServiceInterface(HelloWorld.class);
       //此处自定义的注册名字就相当于注解了，未来迭代的时候会加入自定义注解方式
        serverFactoryBean.setServiceName("hello");
        serverFactoryBean.setServiceImpl(hello());
        serverFactoryBean.setZkConn("127.0.0.1:2181");

        new Thread(() -> {
            try {
                serverFactoryBean.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "RpcServer").start();
        return serverFactoryBean;
    }

    public static void main(String[] args) {
       
        SpringApplication.run(SpringServerConfig.class, "--server.port=8082");
    }
}

```



##### 编写服务调用端启动类:

###### `SpringClientConfig`:

```java
package com.nia.rpc.example.client;

import com.nia.rpc.example.service.HelloWorld;
import com.nia.rpc.factory.ClientFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
@Configuration
@RestController
@SpringBootApplication
@RequestMapping("/test")
public class SpringClientConfig {
    @Bean
    public HelloWorld clientFactoryBean() throws Exception {
        ClientFactoryBean<HelloWorld> clientFactoryBean = new ClientFactoryBean<>();
        clientFactoryBean.setZkConn("127.0.0.1:2181");
        clientFactoryBean.setServiceName("hello");
        clientFactoryBean.setServiceInterface(HelloWorld.class);
      return clientFactoryBean.getObject();
    }
    @Resource
    private HelloWorld helloWorld;

    @RequestMapping("/hello")
    public String hello(String say) {
        return helloWorld.say(say);
    }
    public static void main(String[] args) {
        SpringApplication.run(SpringClientConfig.class, "--server.port=8081");
    }
}

```

测试截图:

![](http://og0sybnix.bkt.clouddn.com/sp170220_022654.png)
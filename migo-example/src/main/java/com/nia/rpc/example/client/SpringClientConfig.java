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
        return helloWorld.say("123");
    }
    public static void main(String[] args) {
        SpringApplication.run(SpringClientConfig.class, "--server.port=8081");
    }
}

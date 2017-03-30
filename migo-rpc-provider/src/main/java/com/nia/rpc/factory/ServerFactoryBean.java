package com.nia.rpc.factory;

import com.nia.rpc.core.bootstrap.ServerBuilder;
import com.nia.rpc.core.server.Server;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

import javax.annotation.PreDestroy;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
@Data
public class ServerFactoryBean implements FactoryBean<Object>{

    private Class<?> serviceInterface;
    private Object serviceImpl;
    private String ip;
    private int port;
    private String serviceName;
    private String zkConn;
    private Server rpcServer;

    //服务注册并提供
    public void start(){
        rpcServer = ServerBuilder
                .builder()
                .serviceImpl(serviceImpl)
                .serviceName(serviceName)
                .zkConn(zkConn)
                .port(port)
                .build();
        rpcServer.start();
    }
    //服务下线
    @PreDestroy
    public void serviceOffline(){
        rpcServer.shutdown();
    }
    @Override
    public Object getObject() throws Exception {
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return this.getClass();
}

    @Override
    public boolean isSingleton() {
        return true;
    }
}

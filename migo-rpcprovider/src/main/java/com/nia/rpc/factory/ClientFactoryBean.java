package com.nia.rpc.factory;

import com.nia.rpc.core.bootstrap.ClientBuilder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
@Data
public class ClientFactoryBean<T> implements FactoryBean<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactoryBean.class);

    private Class<T> serviceInterface;
    private String serviceName;
    private String zkConn;

    @Override
    public T getObject() throws Exception {
        return ClientBuilder
                .<T>builder()
                .zkConn(zkConn)
                .serviceName(serviceName)
                .serviceInterface(serviceInterface)
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

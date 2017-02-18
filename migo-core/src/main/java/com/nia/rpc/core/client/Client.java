package com.nia.rpc.core.client;

import com.nia.rpc.core.protocol.Response;

import java.lang.reflect.Method;

/**
 * 服务的发现与使用
 *
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
public interface Client {
    Response sendMessage(Class<?> clazz, Method method, Object[] args);
    <T> T proxyInterface(Class<T> serviceInterface);
    void close();
}

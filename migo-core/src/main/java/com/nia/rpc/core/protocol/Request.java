package com.nia.rpc.core.protocol;

import lombok.Data;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
@Data
public class Request {
    private long requestId;
    private Class<?> clazz;
    private String method;
    private Class<?>[] parameterTypes;
    private Object[] params;
    private long requestTime;

}

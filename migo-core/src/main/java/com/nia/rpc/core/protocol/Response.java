package com.nia.rpc.core.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
@Setter
@Getter
public class Response {
    private long requestId;
    private Object response;
    private Throwable throwable;
}

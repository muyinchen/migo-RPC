package com.nia.rpc.core.exception;

/**
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
public class RequestTimeoutException extends RuntimeException {

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }
}

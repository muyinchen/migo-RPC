package com.nia.rpc.core.utils;

import com.nia.rpc.core.protocol.Response;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
public class ResponseMapHelper {
    public static ConcurrentMap<Long, BlockingQueue<Response>> responseMap = new ConcurrentHashMap<>();
}



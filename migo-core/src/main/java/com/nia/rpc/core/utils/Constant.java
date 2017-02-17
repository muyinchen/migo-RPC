package com.nia.rpc.core.utils;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 5000;

    int MAX_FRAME_LENGTH = 1024 * 1024; // 1MB

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/services/";
}

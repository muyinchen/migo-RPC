package com.nia.rpc.core.serializer;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
public interface Serializer {
    byte[] serialize(Object obj);
    <T> T deserialize(byte[] bytes);
}

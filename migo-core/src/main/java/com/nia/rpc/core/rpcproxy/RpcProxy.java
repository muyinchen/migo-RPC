package com.nia.rpc.core.rpcproxy;

import com.nia.rpc.core.client.Client;

/**
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
public interface RpcProxy {
    <T> T proxyInterface(Client client, final Class<T> serviceInterface);
}

package com.nia.rpc.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP and Port Helper for RPC
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
public class NetUtils {

    /**
     * 此处实现的并不到位，暂时就这样处理的
     * 用Java获取本机IP地址，需要处理：
     *1. 多块网卡。
     *2. 排除loopback设备、虚拟网卡
     *看似简单的代码，写起来还是要小心一些的。
     * @return
     */
    public static String getLocalIp() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }


}

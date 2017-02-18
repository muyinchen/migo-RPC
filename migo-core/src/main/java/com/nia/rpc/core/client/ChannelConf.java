package com.nia.rpc.core.client;

import com.nia.rpc.core.utils.ConnectionObjectFactory;
import io.netty.channel.Channel;
import lombok.Data;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
@Data
public class ChannelConf {
    private String connStr;
    private String host;
    private int ip;
    private Channel channel;
    private ObjectPool<Channel> channelObjectPool;

    public ChannelConf(String host, int port) {
        this.host = host;
        this.ip = port;
        this.connStr = host + ":" + ip;
        channelObjectPool = new GenericObjectPool<Channel>(new ConnectionObjectFactory(host, port));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChannelConf{");
        sb.append("connStr='").append(connStr).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", ip=").append(ip);
        sb.append(", channel=").append(channel);
        sb.append(", channelObjectPool=").append(channelObjectPool);
        sb.append('}');
        return sb.toString();
    }
}

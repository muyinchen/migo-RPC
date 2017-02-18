package com.nia.rpc.core.server;

import com.nia.rpc.core.protocol.RpcDecoder;
import com.nia.rpc.core.protocol.RpcEncoder;
import com.nia.rpc.core.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.nia.rpc.core.utils.Constant.ZK_DATA_PATH;

/**
 * Author  知秋
 * Created by Auser on 2017/2/17.
 */
public class ServerImpl implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerImpl.class);

    private String localIp;
    private int port;
    private boolean started = false;
    private Channel channel;
    private Object serviceImpl;
    private String serviceName;
    private String zkConn;
    private String serviceRegisterPath;

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private CuratorFramework curatorFramework;

    public ServerImpl(int port, Object serviceImpl, String serviceName) {
        this.port = port;
        this.serviceImpl = serviceImpl;
        this.serviceName = serviceName;
    }

    public ServerImpl(int port, Object serviceImpl, String serviceName, String zkConn) {
        this.port = port;
        this.serviceImpl = serviceImpl;
        this.serviceName = serviceName;
        this.zkConn = zkConn;
    }

    @Override
    public void start() {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                       .channel(NioServerSocketChannel.class)
                       .childHandler(new ChannelInitializer<SocketChannel>() {
                           @Override
                           protected void initChannel(SocketChannel socketChannel) throws Exception {
                               socketChannel.pipeline()
                                            .addLast(new LoggingHandler(LogLevel.INFO))
                                            .addLast(new RpcDecoder(10 * 1024 * 1024))
                                            .addLast(new RpcEncoder())
                                            .addLast(new RpcServerHandler(serviceImpl));
                           }
                       });
        try {
            //调用bind等待客户端来连接
            ChannelFuture future = serverBootstrap.bind(port).sync();
            //接着注册服务
            registerService();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void registerService() {
         zkConn = getZkConn();
         localIp = NetUtils.getLocalIp();
        String serviceIp=localIp+":"+port;
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(zkConn,
                new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();
        //连接上zk然后开始注册服务节点
        String serviceBasePath=ZK_DATA_PATH+serviceName;
        //添加基础服务节点
        try {
            curatorFramework.create()
                            .creatingParentContainersIfNeeded()
                            .forPath(serviceBasePath);
        } catch (Exception e) {
            if (e.getMessage().contains("NodeExist")) {
                LOGGER.info("This Path Service has already Exist");
            } else {
                LOGGER.error("Create Path Error ", e);
                throw new RuntimeException("Register error");
            }
        }

        boolean registerSuccess=false;

        //如果添加成功，添加标识服务具体路径的节点
        while (!registerSuccess){
            try {
                curatorFramework.create()
                                .withMode(CreateMode.EPHEMERAL)
                                .forPath(serviceBasePath+"/"+serviceIp);
            } catch (Exception e) {
                //出错重新注册(要先删除下节点再重新注册)
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                LOGGER.info("Retry Register ZK, {}", e.getMessage());
                try {
                    curatorFramework.delete().forPath(serviceBasePath + "/" + serviceIp);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        }

    }

    @Override
    public void shutdown() {
        //关停相关服务的逻辑
        LOGGER.info("Shutting down server {}", serviceName);
        unRegister();
        if (curatorFramework != null) {
            curatorFramework.close();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private void unRegister() {
        LOGGER.info("unRegister zookeeper");
        try {
            curatorFramework.delete().forPath(ZK_DATA_PATH+serviceName+"/"+localIp+":"+port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getZkConn() {
        return zkConn;
    }

    public void setZkConn(String zkConn) {
        this.zkConn = zkConn;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}

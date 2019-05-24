package com.nia.rpc.core.client;

import com.google.common.base.Splitter;
import com.nia.rpc.core.exception.RequestTimeoutException;
import com.nia.rpc.core.protocol.Request;
import com.nia.rpc.core.protocol.Response;
import com.nia.rpc.core.rpcproxy.CglibRpcProxy;
import com.nia.rpc.core.rpcproxy.RpcProxy;
import com.nia.rpc.core.utils.ResponseMapHelper;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.nia.rpc.core.utils.Constant.ZK_DATA_PATH;

/**
 * Author  知秋
 * Created by Auser on 2017/2/18.
 */
public class ClientImpl implements Client{
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientImpl.class);

    private static AtomicLong atomicLong = new AtomicLong();
    // 通过此发布的服务名称,来寻找对应的服务提供者
    private String serviceName;
    private int requestTimeoutMillis = 10 * 1000;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);
    private String zkConn;
    private CuratorFramework curatorFramework;
    private Class<? extends RpcProxy> clientProxyClass;
    private RpcProxy rpcProxy;

    // 存放ChannelConf到一个CopyOnWriteArrayList中，这个本就是读多写少的场景(服务注册后很少会发生状态改变)，所以很适用
    public static CopyOnWriteArrayList<ChannelConf> channelWrappers = new CopyOnWriteArrayList<>();

    public ClientImpl(String serviceName) {
        this.serviceName = serviceName;
    }

    public void init() {


        // 注册中心不可用时,保存本地缓存

        curatorFramework = CuratorFrameworkFactory.newClient(getZkConn(), new ExponentialBackoffRetry(1000, 3));
        curatorFramework.start();


        final GetChildrenBuilder children = curatorFramework.getChildren();
        try {
            final String serviceZKPath = ZK_DATA_PATH + serviceName;
            //通过curator API 的Path Cache用来监控一个ZNode的子节点.
            // 当一个子节点增加， 更新，删除时， Path Cache会改变它的状态，
            // 会包含最新的子节点， 子节点的数据和状态。
            // 这也正如它的名字表示的那样， 那监控path。
            PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, serviceZKPath, true);
            pathChildrenCache.start();

            pathChildrenCache.getListenable().addListener((client, event) -> {
                LOGGER.info("Listen Event {}", event);
                //通过路径拿到此节点下可以提供服务的实现类节点连接地址
                List<String> newServiceData = children.forPath(serviceZKPath);
                LOGGER.info("Server {} list change {}", serviceName, newServiceData);

                // 关闭删除本地缓存中多出的channel

                for (ChannelConf cw : channelWrappers) {
                    String connStr = cw.getConnStr();
                    if (!newServiceData.contains(connStr)) {
                        cw.close();
                        LOGGER.info("Remove channel {}", connStr);
                        channelWrappers.remove(cw);
                    }
                }

                // 增加本地缓存中不存在的连接地址
                for (String connStr : newServiceData) {
                    boolean containThis = false;
                    for (ChannelConf cw : channelWrappers) {
                        if (connStr != null && connStr.equals(cw.getConnStr())) {
                            containThis = true;
                        }
                    }
                    if (!containThis) {
                        addNewChannel(connStr);
                    }
                }
            });

            List<String> strings = children.forPath(serviceZKPath);
            if (CollectionUtils.isEmpty(strings)) {
                throw new RuntimeException("No Service available for " + serviceName);
            }

            LOGGER.info("Found Server {} List {}", serviceName, strings);
            for (String connStr : strings) {
                try {
                    addNewChannel(connStr);
                } catch (Exception e) {
                    LOGGER.error("Add New Channel Exception", e);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addNewChannel(String connStr) {
        try {
            List<String> strings = Splitter.on(":").splitToList(connStr);
            if (strings.size() != 2) {
                throw new RuntimeException("Error connection str " + connStr);
            }
            String host = strings.get(0);
            int port = Integer.parseInt(strings.get(1));
            ChannelConf channelWrapper = new ChannelConf(host, port);
            channelWrappers.add(channelWrapper);
            LOGGER.info("Add New Channel {}, {}", connStr, channelWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ChannelConf selectChannel() {
        Random random = new Random();
        //同一个服务下有好几个链接地址的实现，那就选一个就是，其实为集群部署考虑，
        // 每一台服务器部署有相同的服务，选择其一来处理即可，假如是nginx代理那就无所谓了
        int size = channelWrappers.size();
        if (size < 1) {
            return null;
        }
        int i = random.nextInt(size);
        return channelWrappers.get(i);
    }

    @Override
    public Response sendMessage(Class<?> clazz, Method method, Object[] args) {

        Request request = new Request();
        request.setRequestId(atomicLong.incrementAndGet());
        request.setMethod(method.getName());
        request.setParams(args);
        request.setClazz(clazz);
        request.setParameterTypes(method.getParameterTypes());

        ChannelConf channelWrapper = selectChannel();

        if (channelWrapper == null) {
            Response response = new Response();
            RuntimeException runtimeException = new RuntimeException("Channel is not active now");
            response.setThrowable(runtimeException);
            return response;
        }
        //当channel的配置链接不为空的时候，就可以取到channel了
        Channel channel = null;
        try {
            channel = channelWrapper.getChannelObjectPool().borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (channel == null) {
            Response response = new Response();
            RuntimeException runtimeException = new RuntimeException("Channel is not available now");
            response.setThrowable(runtimeException);
            return response;
        }


        try {
            BlockingQueue<Response> blockingQueue = new ArrayBlockingQueue<>(1);
            ResponseMapHelper.responseMap.put(request.getRequestId(), blockingQueue);
            channel.writeAndFlush(request);
            //建立一个ResponseMap，将RequestId作为键，服务端回应的内容作为值保存于BlockingQueue，
            // 最后一起保存在这个ResponseMap中
            //poll(time):取走BlockingQueue里排在首位的对象,若不能立即取出,则可以等time参数规定的时间,取不到时返回null

            return blockingQueue.poll(requestTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RequestTimeoutException("service" + serviceName + " method " + method + " timeout");
        } finally {
            try {
                //拿出去的channel记得还回去
                channelWrapper.getChannelObjectPool().returnObject(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //删除此键值对，help GC
            ResponseMapHelper.responseMap.remove(request.getRequestId());
        }
    }

    @Override
    public <T> T proxyInterface(Class<T> serviceInterface) {
        if (clientProxyClass == null) {
            clientProxyClass = CglibRpcProxy.class;
        }
        try {
            rpcProxy = clientProxyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return rpcProxy.proxyInterface(this, serviceInterface);
    }

    @Override
    public void close() {
        //注意要关三处地方，一个是先关闭zookeeper的连接，另一个是channel池对象，最后是netty的断开关闭
        if (curatorFramework != null) {
            curatorFramework.close();
        }
        try {
            for (ChannelConf cw : channelWrappers) {
                cw.close();
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public String getZkConn() {
        return zkConn;
    }

    public void setZkConn(String zkConn) {
        this.zkConn = zkConn;
    }

    public int getRequestTimeoutMillis() {
        return requestTimeoutMillis;
    }

    public void setRequestTimeoutMillis(int requestTimeoutMillis) {
        this.requestTimeoutMillis = requestTimeoutMillis;
    }
}

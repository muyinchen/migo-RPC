import com.nia.rpc.core.bootstrap.ServerBuilder;
import com.nia.rpc.core.server.Server;
import com.nia.rpc.core.server.ServerImpl;
import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

/**
 * Author  知秋
 * Created by Auser on 2017/2/19.
 */
@Data
public class ServerFactory implements FactoryBean<Object>{

    private Class<?> serviceInterface;
    private Object serviceImpl;
    private String ip;
    private int port;
    private String serviceName;
    private String zkConn;
    private ServerImpl rpcServer;

    //服务注册并提供
    public void start(){
        Server rpcServer = ServerBuilder
                .builder()
                .serviceImpl(serviceImpl)
                .serviceName(serviceName)
                .zkConn(zkConn)
                .port(port)
                .build();
        rpcServer.start();
    }
    //服务下线
    public void serviceOffline(){
        rpcServer.shutdown();
    }
    @Override
    public Object getObject() throws Exception {
        return this;
    }

    @Override
    public Class<?> getObjectType() {
        return this.getClass();
}

    @Override
    public boolean isSingleton() {
        return true;
    }
}

package test.chao.pa.netty.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.netty.server.NettyServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiezhengchao
 * @since 2020/6/24 15:49
 */
public class ServerProxy {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String,NettyServer> serviceNameMap = new HashMap<>();
	private List<NettyServer> nettyServerList = new ArrayList<>();

	public void publish(String serviceName,Object service,int port){
		NettyServer nettyServer = new NettyServer(this,port,serviceName,service);
		nettyServer.run();


		serviceNameMap.put(serviceName, nettyServer);
		logger.info("serviceName:{} 发布成功...",serviceName);
	}

	public void destroy(){
		nettyServerList.forEach(NettyServer::shutdown);
	}

	public Map<String, NettyServer> getServiceNameMap() {
		return serviceNameMap;
	}
}

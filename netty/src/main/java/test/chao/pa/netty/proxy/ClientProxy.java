package test.chao.pa.netty.proxy;

import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.netty.ContextStore;
import test.chao.pa.netty.NettyClient;
import test.chao.pa.netty.RootProtoGen;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xiezhengchao
 * @since 2020/6/24 15:48
 */
@SuppressWarnings("unchecked")
public class ClientProxy implements InvocationHandler {

	private static final Logger logger = LoggerFactory.getLogger(ClientProxy.class);
	private static Map<String,Object> proxyMap = new HashMap<>();
	private static AtomicInteger atomicInteger = new AtomicInteger(0);
	private static final ThreadLocal<Future<Object>> ASYNC_RESULT = new ThreadLocal<>();

	private Class<?> clz;
	private NettyClient nettyClient;
	private boolean async;

	public ClientProxy(Class<?> clz, NettyClient nettyClient, boolean async) {
		this.clz = clz;
		this.nettyClient = nettyClient;
		this.async = async;
	}

	public static <T> T newSyncProxy(Class<T> clz, String host, int port){
		Object proxy = proxyMap.get(clz.getName());
		if(proxy == null){
			proxy = Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientProxy(clz,new NettyClient(host,port),false));
			proxyMap.put(clz.getName(),proxy);
		}
		logger.info("ClientProxy:{} newSyncProxy...",clz.getName());
		return (T) proxy;
	}


	public static <T> T newAsyncProxy(Class<T> clz, String host, int port){
		Object proxy = proxyMap.get(clz.getName());
		if(proxy == null){
			proxy = Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientProxy(clz,new NettyClient(host,port),true));
			proxyMap.put(clz.getName(),proxy);
		}
		logger.info("ClientProxy:{} newAsyncProxy...",clz.getName());
		return (T) proxy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		int seq = atomicInteger.getAndIncrement();

		List<RootProtoGen.TypeValue> typeValueList = Stream.of(args).map(param-> RootProtoGen.TypeValue.newBuilder().setType(param.getClass().getName())
				.setValue(ByteString.copyFrom(nettyClient.getSerializeAbility().encode(param))).build()).collect(Collectors.toList());


		RootProtoGen.RequestContext request =RootProtoGen.RequestContext.newBuilder().setSeq(seq)
				.setClzName(clz.getName()).setMethodName(method.getName()).addAllMethodParam(typeValueList).build();

		nettyClient.sent(request);

		if(!async){
			return nettyClient.getResponse(seq);
		}

		SettableFuture future = SettableFuture.create();
		ContextStore.getResponseMap().putIfAbsent(seq, future);
		ASYNC_RESULT.set(future);
		return null;
	}

	public static <T> Future<T> getAsyncFuture(){
		return (Future<T>) ASYNC_RESULT.get();
	}

}

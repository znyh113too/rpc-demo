package test.chao.pa.netty.server;

import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.chao.pa.netty.RootProtoGen;
import test.chao.pa.netty.codec.SerializeAbility;
import test.chao.pa.netty.proxy.ServerProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author xiezhengchao
 * @since 2020/6/24 16:28
 */
public class DefaultServerHandler extends SimpleChannelInboundHandler<RootProtoGen.RequestContext> {

	private ServerProxy serverProxy;
	// 序列化方式应当从协议头中获取
	private SerializeAbility serializeAbility;

	public DefaultServerHandler(ServerProxy serverProxy, SerializeAbility serializeAbility) {
		this.serverProxy = serverProxy;
		this.serializeAbility = serializeAbility;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RootProtoGen.RequestContext msg) throws Exception {

		NettyServer nettyServer = serverProxy.getServiceNameMap().get(msg.getClzName());
		if(nettyServer==null){
			throw new RuntimeException("没有这个方法");
		}

		nettyServer.submit(()-> handle(nettyServer.getServiceObj(),ctx,msg));
	}

	private void handle(Object obj,ChannelHandlerContext ctx, RootProtoGen.RequestContext msg){

		Class<?> clz = obj.getClass();

		Method method = findMethod(clz,msg);

		Object result = doInvoker(method,obj,findMethodParamValue(msg.getMethodParamList()));

		encodeAndFlush(result,ctx,msg);
	}

	private void encodeAndFlush(Object obj,ChannelHandlerContext ctx, RootProtoGen.RequestContext msg){

		RootProtoGen.TypeValue respTypeValue = RootProtoGen.TypeValue.newBuilder()
				.setType(obj.getClass().getName()).setValue(ByteString.copyFrom(serializeAbility.encode(obj))).build();

		RootProtoGen.ResponseContext responseContext = RootProtoGen.ResponseContext.newBuilder().setSeq(msg.getSeq())
				.setResponseValue(respTypeValue).build();

		ctx.writeAndFlush(responseContext);
	}

	private Method findMethod(Class<?> clz,RootProtoGen.RequestContext msg){
		try {
			return clz.getDeclaredMethod(msg.getMethodName(), findMethodParamType(msg.getMethodParamList()));
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	private Class[] findMethodParamType(List<RootProtoGen.TypeValue> typeValueList){
		if(typeValueList==null || typeValueList.isEmpty()){
			return new Class[]{};
		}
		return typeValueList.stream().map(RootProtoGen.TypeValue::getType).map(s->{
			try {
				return Class.forName(s);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}).toArray(Class[]::new);
	}

	private Object[] findMethodParamValue(List<RootProtoGen.TypeValue> typeValueList){
		if(typeValueList==null || typeValueList.isEmpty()){
			return new Object[]{};
		}
		return typeValueList.stream().map(typeValue-> serializeAbility.decode(typeValue.getType(),typeValue.getValue().toByteArray())).toArray(Object[]::new);
	}

	private Object doInvoker(Method method,Object obj, Object... args){
		try {
			return method.invoke(obj, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}

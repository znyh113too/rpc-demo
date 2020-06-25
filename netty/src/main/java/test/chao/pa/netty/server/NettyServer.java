package test.chao.pa.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.netty.codec.ProtoBufSer;
import test.chao.pa.netty.codec.SerializeAbility;
import test.chao.pa.netty.proxy.ServerProxy;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiezhengchao
 * @since 2020/6/24 16:09
 */
public class NettyServer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/** required */
	private ServerProxy serverProxy;
	private int port;
	private String serviceName;
	private Object serviceObj;

	public NettyServer(ServerProxy serverProxy, int port, String serviceName, Object serviceObj) {
		this.serverProxy = serverProxy;
		this.port = port;
		this.serviceName = serviceName;
		this.serviceObj = serviceObj;
	}

	/** own */
	private SerializeAbility serializeAbility = new ProtoBufSer();
	private ThreadPoolExecutor threadPool;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel ch;
	private boolean isShutdown = false;


	public void run(){
		initThreadPool();
		try {
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup(4);
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<Channel>() {
						@Override
						protected void initChannel(Channel ch) throws Exception {
							ch.pipeline()
									.addLast(new DefaultServerFrameDecoder(serializeAbility))
									.addLast(new DefaultServerFrameEncoder())
									.addLast(new DefaultServerHandler(serverProxy,serializeAbility));
						}
					})
					.option(ChannelOption.SO_REUSEADDR, true);

			ch = b.bind(port).sync().channel();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void submit(Runnable r){
		threadPool.execute(r);
	}

	public Object getServiceObj() {
		return serviceObj;
	}

	private void initThreadPool() {
		threadPool = new ThreadPoolExecutor(5, 10, 30L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(1000), new CustomThreadFactory(serviceName));
		threadPool.prestartAllCoreThreads();;
	}

	public void shutdown() {
		if (!isShutdown) {
			isShutdown=true;
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			threadPool.shutdown();
		}
	}

	private static class CustomThreadFactory implements ThreadFactory{
		private static AtomicInteger atomicInteger = new AtomicInteger(1);

		private String threadPoolName;

		public CustomThreadFactory(String threadPoolName) {
			this.threadPoolName = threadPoolName;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(Thread.currentThread().getThreadGroup(),r,String.format("rpc-%s-%s-workThread",threadPoolName,atomicInteger.getAndIncrement()));
		}
	}
}

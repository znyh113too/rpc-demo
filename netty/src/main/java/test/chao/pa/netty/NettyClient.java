package test.chao.pa.netty;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import test.chao.pa.netty.codec.ProtoBufSer;
import test.chao.pa.netty.codec.SerializeAbility;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * @author xiezhengchao
 * @since 2020/6/24 18:30
 */
public class NettyClient {
	private final String host;
	private final int port;
	private Channel channel;

	private SerializeAbility serializeAbility = new ProtoBufSer();

	public NettyClient(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sent(final RootProtoGen.RequestContext request) throws InterruptedException {
		channel.writeAndFlush(request);
	}

	public Object getResponse(final int messageId) {
		ContextStore.getResponseMap().putIfAbsent(messageId, SettableFuture.create());
		try {
			return ContextStore.getResponseMap().get(messageId).get();
		} catch (final InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			ContextStore.getResponseMap().remove(messageId);
		}
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap b = new Bootstrap();
		b.group(group)
				.channel(NioSocketChannel.class)
				.remoteAddress(new InetSocketAddress(host, port))
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch)
							throws Exception {
						ch.pipeline().addLast(
								new DefaultClientFrameDecoder(serializeAbility),
								new DefaultClientFrameEncoder(),
								new DefaultClientHandler());
					}
				});

		ChannelFuture f = b.connect().sync();
		channel = f.channel();
	}

	public SerializeAbility getSerializeAbility() {
		return serializeAbility;
	}
}

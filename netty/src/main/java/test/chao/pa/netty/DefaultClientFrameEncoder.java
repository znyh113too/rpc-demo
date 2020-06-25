package test.chao.pa.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author xiezhengchao
 * @since 2020/6/24 18:29
 */
public class DefaultClientFrameEncoder extends MessageToByteEncoder<RootProtoGen.RequestContext> {
	@Override
	protected void encode(ChannelHandlerContext ctx, RootProtoGen.RequestContext msg, ByteBuf out) throws Exception {
		CustomTFrameProtocol.encode(out,msg);
	}
}

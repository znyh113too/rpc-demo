package test.chao.pa.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import test.chao.pa.netty.CustomTFrameProtocol;
import test.chao.pa.netty.RootProtoGen;

/**
 *
 * @author xiezhengchao
 * @since 2020/6/24 16:23
 */
public class DefaultServerFrameEncoder extends MessageToByteEncoder<RootProtoGen.ResponseContext>{
	@Override
	protected void encode(ChannelHandlerContext ctx, RootProtoGen.ResponseContext msg, ByteBuf out) throws Exception {
		CustomTFrameProtocol.encode(out,msg);
	}
}

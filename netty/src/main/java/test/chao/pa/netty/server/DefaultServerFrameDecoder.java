package test.chao.pa.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import test.chao.pa.netty.CustomTFrameProtocol;
import test.chao.pa.netty.RootProtoGen;
import test.chao.pa.netty.codec.SerializeAbility;

import java.util.List;

/**
 * @author xiezhengchao
 * @since 2020/6/24 16:24
 */
public class DefaultServerFrameDecoder extends ByteToMessageDecoder {

	private SerializeAbility serializeAbility;

	public DefaultServerFrameDecoder(SerializeAbility serializeAbility) {
		this.serializeAbility = serializeAbility;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

		if (byteBuf.readableBytes() < 10){
			return;
		}

		byteBuf.markReaderIndex();
		byte[] first4bytes = new byte[4];
		byteBuf.readBytes(first4bytes,0, 4);
		if (first4bytes[0] == CustomTFrameProtocol.first && first4bytes[1] == CustomTFrameProtocol.second) {
			byte[] i32buf = new byte[4];
			byteBuf.readBytes(i32buf,0, 4);
			int bodyLength = CustomTFrameProtocol.decodeFrameSize(i32buf, 0, 4);

			if (bodyLength < 0) {
				ctx.close();
			}

			if (byteBuf.readableBytes() < bodyLength) {
				byteBuf.resetReaderIndex();
				return;
			}

			byte[] body = new byte[bodyLength];
			byteBuf.readBytes(body,0,bodyLength);

			Object request = serializeAbility.decode(RootProtoGen.RequestContext.class.getName(),body);

			out.add(request);
		}
	}
}

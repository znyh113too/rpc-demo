package test.chao.pa.netty;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import test.chao.pa.netty.codec.SerializeAbility;

import java.util.List;

/**
 * @author xiezhengchao
 * @since 2020/6/24 16:24
 */
public class DefaultClientFrameDecoder extends ByteToMessageDecoder {

	private SerializeAbility serializeAbility;

	public DefaultClientFrameDecoder(SerializeAbility serializeAbility) {
		this.serializeAbility = serializeAbility;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

		if (byteBuf.readableBytes() < 10){
			return;
		}

		int bodyLength;
		byte[] first4bytes = new byte[4];
		byteBuf.markReaderIndex();
		// byteBuf.getBytes(readerIndex, first4bytes);
		byteBuf.readBytes(first4bytes,0, 4);
		if (first4bytes[0] == CustomTFrameProtocol.first && first4bytes[1] == CustomTFrameProtocol.second) {
			byte[] i32buf = new byte[4];
			//byteBuf.getBytes(readerIndex + 4, i32buf);
			byteBuf.readBytes(i32buf,0, 4);
			bodyLength = CustomTFrameProtocol.decodeFrameSize(i32buf, 0, 4);

			if (bodyLength < 0) {
				ctx.close();
			}

			// if (byteBuf.readableBytes() < bodyLength + 8) {
			if (byteBuf.readableBytes() < bodyLength) {
				byteBuf.resetReaderIndex();
				return;
			}

			// byteBuf.readBytes(8);
			byte[] body = new byte[bodyLength];
			byteBuf.readBytes(body,0,bodyLength);

			Object response = serializeAbility.decode(RootProtoGen.ResponseContext.class.getName(),body);

			RootProtoGen.ResponseContext responseContext = (RootProtoGen.ResponseContext) response;

			SettableFuture<Object> future = ContextStore.getResponseMap().get(responseContext.getSeq());
			future.set(serializeAbility.decode(responseContext.getResponseValue().getType(),responseContext.getResponseValue().getValue().toByteArray()));

			out.add(responseContext);
		}
	}
}

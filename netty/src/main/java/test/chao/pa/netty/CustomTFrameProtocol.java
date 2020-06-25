package test.chao.pa.netty;

import io.netty.buffer.ByteBuf;

/**
 * 可变帧自定义协议
 * 协议格式:
 1B	  | 1B	 |  1B	    |1B	        |4B	           |total length - 8B
 0xFF |	0xCC |	version	|protocol	|total length  |body
 */
public class CustomTFrameProtocol {

	public static final byte first = (byte) 0xFF;
	public static final byte second = (byte) 0xCC;
	public static final byte version = (byte) 0xEE;
	public static final byte protocol = (byte) 0xEE;

	public static int decodeFrameSize(final byte[] buf, final int pos, final int length){
		if(2 != length && 4 != length){
			throw new RuntimeException("decodeFrameSize must be 2 or 4");
		}
		if(pos < 0 || buf.length < pos + length ){
			throw new RuntimeException("decodeFrameSize out of array index of buf");
		}

		int size = 0;
		for (int i = 0, j = 8 * (length-1); i < length; i++, j = j - 8) {
			size = size | ((buf[i + pos] & 0xff) << j);
		}
		return size;
	}

	public static void encode(ByteBuf byteBuf, RootProtoGen.RequestContext requestContext){
		fixed(byteBuf);

		int totalSize = requestContext.getSerializedSize();
		byteBuf.writeInt(totalSize);
		byteBuf.writeBytes(requestContext.toByteArray());
	}

	public static void encode(ByteBuf byteBuf, RootProtoGen.ResponseContext responseContext){
		fixed(byteBuf);

		int totalSize = responseContext.getSerializedSize();
		byteBuf.writeInt(totalSize);
		byteBuf.writeBytes(responseContext.toByteArray());
	}

	private static void fixed(ByteBuf byteBuf){
		byteBuf.writeByte(first);
		byteBuf.writeByte(second);
		byteBuf.writeByte(version);
		byteBuf.writeByte(protocol);
	}
}

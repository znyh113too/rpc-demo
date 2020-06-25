package test.chao.pa.netty.codec;

import com.google.common.collect.ImmutableMap;
import io.netty.util.CharsetUtil;

import java.util.Map;

/**
 * 基础类型
 * @author xiezhengchao
 * @since 2020/6/24 19:02
 */
public class BaseSer implements SerializeAbility {
	/** 举几个简单的例子 */
	private Map<String,Class<?>> nameClzMap = ImmutableMap.<String,Class<?>>builder()
			.put(String.class.getName(),String.class)
			.put(Integer.class.getName(),Integer.class)
			.put(Long.class.getName(),Long.class)
			.build();

	@Override
	public Object decode(String clzName, byte[] bytes) {
		Class<?> clz = nameClzMap.get(clzName);
		if(clz!=null){
			// 是不是有更好的方法 这么写很不对劲
			String sv = new String(bytes, CharsetUtil.UTF_8);
			if(clz.isAssignableFrom(Integer.class)){
				return Integer.parseInt(sv);
			}
			if(clz.isAssignableFrom(Long.class)){
				return Long.parseLong(sv);
			}
			return sv;
		}
		return null;
	}

	@Override
	public byte[] encode(Object obj) {
		Class<?> clz = nameClzMap.get(obj.getClass().getName());
		if(clz!=null){
			// 可以更细致一点
			return obj.toString().getBytes(CharsetUtil.UTF_8);
		}
		return null;
	}
}

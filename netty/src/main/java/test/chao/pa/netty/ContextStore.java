package test.chao.pa.netty;

import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiezhengchao
 * @since 2020/6/24 18:35
 */
public class ContextStore {

	private static final ConcurrentHashMap<Integer, SettableFuture<Object>> RESPONSE_MAP = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<Integer, SettableFuture<Object>> getResponseMap() {
		return RESPONSE_MAP;
	}
}

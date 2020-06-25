package test.chao.pa.netty.codec;

/**
 * 序列化部分
 * 
 * @author xiezhengchao
 * @since 2020/6/24 11:18
 */
public interface SerializeAbility {

    Object decode(String clzName, byte[] bytes);

    byte[] encode(Object obj);
}

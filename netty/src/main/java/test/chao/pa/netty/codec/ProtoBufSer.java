package test.chao.pa.netty.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author xiezhengchao
 * @since 2020/6/24 11:20
 */
public class ProtoBufSer extends BaseSer{

    @Override
    public Object decode(String clzName, byte[] bytes) {
	    Object baseObj = super.decode(clzName,bytes);
    	if(baseObj!=null){
    		return baseObj;
	    }

        try {
            Class<?> clz = Class.forName(clzName);
            Method method = clz.getDeclaredMethod("parseFrom", byte[].class);
            return method.invoke(null, bytes);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encode(Object obj) {
	    byte[] baseBytes = super.encode(obj);
	    if(baseBytes!=null){
		    return baseBytes;
	    }

        try {
            Class<?> clz = obj.getClass();
            Method method = clz.getMethod("toByteArray");
            return (byte[]) method.invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

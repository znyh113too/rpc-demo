package test.chao.pa.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.client.PersonService;
import test.chao.pa.netty.proxy.ServerProxy;

import java.util.concurrent.TimeUnit;

/**
 * @author xiezhengchao
 * @since 2020/6/24 18:06
 */
public class NettyServerMain {

	private static final Logger logger = LoggerFactory.getLogger(NettyServerMain.class);

	public static void main(String[] args) {
		ServerProxy serverProxy = new ServerProxy();
		try{
			serverProxy.publish(PersonService.class.getName(),new PersonServiceImpl(),8088);

			logger.info("NettyServerMain 启动成功...");

			TimeUnit.DAYS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			serverProxy.destroy();
		}
	}
}

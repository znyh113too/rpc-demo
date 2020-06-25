package test.chao.pa.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.client.dto.ProtoGen;
import test.chao.pa.netty.proxy.ClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author xiezhengchao
 * @since 2020/6/24 19:12
 */
public class NettyClientMain {
	private static final Logger logger = LoggerFactory.getLogger(NettyClientMain.class);

	public static void main(String[] args) {
		NettyClientMain nettyClientMain = new NettyClientMain();

		// nettyClientMain.sync();

		nettyClientMain.async();

	}

	private void sync(){
		PersonService personService = ClientProxy.newSyncProxy(PersonService.class,"127.0.0.1",8088);

		for(int i=0;i<10000;i++){
			ProtoGen.Person person = personService.getById(i);
			logger.info("i:{} person:{}",i,person);
		}
	}

	private void async(){
		PersonService personService = ClientProxy.newAsyncProxy(PersonService.class,"127.0.0.1",8088);

		List<Future<ProtoGen.Person>> futureList = new ArrayList<>(10000);
		for(int i=0;i<10000;i++){
			personService.getById(i);
			futureList.add(ClientProxy.getAsyncFuture());
		}

		futureList.forEach(future->{
			try {
				logger.info("async person:{}",future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		});
	}
}

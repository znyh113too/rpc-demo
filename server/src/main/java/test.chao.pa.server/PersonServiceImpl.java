package test.chao.pa.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.chao.pa.client.PersonService;
import test.chao.pa.client.dto.ProtoGen;

/**
 * @author xiezhengchao
 * @since 2020/6/24 15:47
 */
public class PersonServiceImpl implements PersonService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public ProtoGen.Person getById(Integer pid) {
    	logger.info("PersonService getById:{}",pid);
        // 举个例子返回对象
        return ProtoGen.Person.newBuilder().setId(pid).setName(pid + "-PersonServiceImpl-name").build();
    }
}

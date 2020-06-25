package test.chao.pa.client;

import test.chao.pa.client.dto.ProtoGen;

/**
 * @author xiezhengchao
 * @since 2020/6/24 15:40
 */
public interface PersonService {

    ProtoGen.Person getById(Integer pid);
}

package pers.zhixilang.rpc.consumer.service;

import pers.zhixilang.rpc.consumer.entity.User;

/**
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:12
 */
public interface IUserservice {

    String insert(User user);

    User get(String id);
}

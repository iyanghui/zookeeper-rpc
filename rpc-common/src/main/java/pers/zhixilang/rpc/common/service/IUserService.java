package pers.zhixilang.rpc.common.service;

import pers.zhixilang.rpc.common.entity.User;

/**
 *
 * @author zhixilang
 * @version 1.0
 * @date 2019-02-27 13:50
 */
public interface IUserService {
    String insert(User user);

    User get(String id);
}

package pers.zhixilang.rpc.provider.service;

import pers.zhixilang.rpc.provider.entity.User;

/**
 * Copyright (C), 2017-2019, 深圳金证引擎科技有限公司
 *
 * @author yanghui
 * @version 1.0
 * @date 2019-02-27 13:50
 */
public interface IUserService {
    String insert(User user);

    User get(String id);
}

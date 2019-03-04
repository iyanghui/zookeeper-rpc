package pers.zhixilang.rpc.provider.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zhixilang.rpc.provider.annotation.RpcService;
import pers.zhixilang.rpc.provider.entity.User;
import pers.zhixilang.rpc.provider.service.IUserService;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zhixilang
 * @version 1.0
 * @date 2019-02-27 13:51
 */
@RpcService
public class UserServiceImpl implements IUserService {

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private Map<String, User> userMap = new HashMap<>();

    @Override
    public String insert(User user) {
        logger.info("新增用户新信息：{}", JSONObject.toJSONString(user));
        userMap.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public User get(String id) {
        logger.info("获取用户id={}信息", id);
        return userMap.get(id);
    }
}

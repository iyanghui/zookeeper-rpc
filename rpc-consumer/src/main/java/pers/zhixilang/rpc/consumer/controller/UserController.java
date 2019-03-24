package pers.zhixilang.rpc.consumer.controller;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pers.zhixilang.rpc.common.entity.User;
import pers.zhixilang.rpc.common.service.IUserService;
import pers.zhixilang.rpc.consumer.utils.IdUtil;

import javax.annotation.Resource;

/**
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:16
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private IUserService userService;

    @Resource
    private IdUtil idUtil;

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String insertUser() {
        User user = new User();
        user.setId(String.valueOf(idUtil.nextId()));
        user.setName("name" + 1);

        String id = userService.insert(user);
        if (null != id) {
            logger.info("插入用户{}成功", id);
        }
        return id;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public User getUser(@RequestParam() String id) {
        User user = userService.get(id);
        logger.info("查询到用户: {}", JSONObject.toJSONString(user));
        return user;
    }
}

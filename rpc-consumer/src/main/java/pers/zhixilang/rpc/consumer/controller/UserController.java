package pers.zhixilang.rpc.consumer.controller;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import pers.zhixilang.rpc.common.entity.User;
import pers.zhixilang.rpc.common.service.IUserService;
import pers.zhixilang.rpc.consumer.utils.IdUtil;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;

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

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public String insertUser() throws InterruptedException {
        int count = 100;
        CountDownLatch countDownLatch = new CountDownLatch(100);

        long start = System.currentTimeMillis();
        logger.info("开始插入：" + start);
        for (int i = 0; i < count; i++) {
            final int finalI = i;
            new Thread(() -> {
                User user = new User();
                user.setId(String.valueOf(idUtil.nextId()));
                user.setName("name" + finalI);

                String id = userService.insert(user);
                logger.info("插入用户{}成功", id);

                countDownLatch.countDown();

            }).start();
        }

        countDownLatch.await();

        logger.info("插入完成：{}", System.currentTimeMillis() - start);

        return null;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String getUser(@RequestParam() String id) {
        User user = userService.get(id);
        logger.info("查询到用户: {}", JSONObject.toJSONString(user));
        return null;
    }
}

package pers.zhixilang.rpc.consumer.utils;

import org.springframework.stereotype.Component;

/**
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:04
 */
@Component
public class IdUtil {
    private SnowFlakeWorker snowFlakeWorker = new SnowFlakeWorker(3, 3);

    public String nextId() {
        return String.valueOf(snowFlakeWorker.nextId());
    }
}

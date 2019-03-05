package pers.zhixilang.rpc.consumer.configuration;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pers.zhixilang.rpc.common.entity.Request;
import pers.zhixilang.rpc.common.entity.Response;
import pers.zhixilang.rpc.consumer.netty.client.NettyClient;
import pers.zhixilang.rpc.consumer.utils.IdUtil;

import javax.annotation.Resource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 动态代理
 * 调用netty客户端方法，完成服务调用
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:12
 */
@Component
public class RpcFactory<T> implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RpcFactory.class);

    @Resource
    private NettyClient nettyClient;

    @Resource
    private IdUtil idUtil;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Request request = new Request();
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setId(idUtil.nextId());

        logger.info("prc调用.{}", JSONObject.toJSONString(request));

        Object result = nettyClient.send(request);

        return JSONObject.parseObject(result.toString(), Response.class);
    }
}

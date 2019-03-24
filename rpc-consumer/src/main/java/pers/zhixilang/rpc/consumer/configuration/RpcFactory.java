package pers.zhixilang.rpc.consumer.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import java.util.Collection;
import java.util.Map;

/**
 * 动态代理
 * 调用netty客户端方法，完成服务调用
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:12
 */
@Component
public class RpcFactory implements InvocationHandler {

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

        logger.info("prc调用请求.{}", JSONObject.toJSONString(request));

        Object result = nettyClient.send(request);
        logger.info("prc调用响应.{}", JSONObject.toJSONString(result));

        Response response = JSON.parseObject(result.toString(), Response.class);

        Class<?> returnType = method.getReturnType();

        if (null != response.getCode() && response.getCode() == 1) {
            throw new Exception(response.getMsg());
        }
        if (returnType.isPrimitive() || String.class.isAssignableFrom(returnType)) {
            return response.getData();
        } else if (Collection.class.isAssignableFrom(returnType)) {
            return JSONArray.parseObject(response.getData().toString(), Object.class);
        } else if (Map.class.isAssignableFrom(returnType)) {
            return JSON.parseObject(response.getData().toString(), Object.class);
        } else {
            if (null != response.getData()) {
                return JSONObject.parseObject(response.getData().toString(), returnType);
            }
            return null;
        }
    }
}

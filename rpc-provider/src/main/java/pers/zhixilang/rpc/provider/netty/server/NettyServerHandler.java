package pers.zhixilang.rpc.provider.netty.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.zhixilang.rpc.provider.entity.Request;
import pers.zhixilang.rpc.provider.entity.Response;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 * @author zhixilang
 * @version 1.0
 * @date 2019-02-27 17:29
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Map<String, Object> serviceMap;

    public NettyServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        logger.info("客户端连接成功: {}", context.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        logger.info("客户端断开连接: {}", context.channel().remoteAddress());
        context.channel().close();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) {
        Request request = JSON.parseObject(msg.toString(), Request.class);

        if ("heartBeat".equals(request.getMethodName())) {
            logger.info("客户端心跳信息..." + context.channel().remoteAddress());
        } else {
            logger.info("rpc客户端请求接口: {}, 请求地址: {}", request.getClassName(), request.getMethodName());

            Response response = new Response();
            response.setRequestID(request.getId());

            try {
                Object res = handler(request);
                response.setData(res);
            } catch (Throwable e) {
                e.printStackTrace();
                response.setCode(1);
                response.setMsg(e.getMessage());
                logger.error("rpc server handler err: " + e);
            }
            context.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        logger.error(cause.getMessage());
        context.channel().close();
    }

    /**
     * 利用反射执行方法
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handler(Request request) throws Throwable{
        String className = request.getClassName();
        Object bean = serviceMap.get(className);
        if (bean != null) {
            Class<?> serviceClass = bean.getClass();

            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();

            Object[] paramters = request.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);

            return method.invoke(bean, getParameters(parameterTypes, paramters));
        } else {
            throw  new Exception("未找到服务接口，请检查配置：className: " + className);
        }
    }

    private Object[] getParameters(Class<?>[] parameterTypes, Object[] parameters) {
        if (parameterTypes == null || parameters == null) {
            return parameters;
        }

        Object[] newParameters = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            newParameters[i] = JSON.parseObject(parameters[i].toString(), parameterTypes[i]);
        }
        return newParameters;
    }
}

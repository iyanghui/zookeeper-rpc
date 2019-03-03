package pers.zhixilang.rpc.provider.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import pers.zhixilang.rpc.provider.annotation.RpcService;
import pers.zhixilang.rpc.provider.registry.ServiceRegistry;
import pers.zhixilang.rpc.provider.netty.codec.json.*;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 开启netty服务监听
 * 将服务地址添加到zookeeper数据节点
 * Copyright (C), 2017-2019, 深圳金证引擎科技有限公司
 *
 * @author yanghui
 * @version 1.0
 * @date 2019-02-27 17:05
 */
@Component
public class NettyServer implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(1);

    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(4);

    /**
     * 存放接口服务类
     */
    private Map<String, Object> serviceMap = new HashMap<>();

    @Value("${rpc.server.address}")
    private String serverAddress;

    @Resource
    private ServiceRegistry serviceRegistry;

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RpcService.class);
        for (Object bean: beans.values()) {
            Class clazz = bean.getClass();
            Class[] interfaces = clazz.getInterfaces();
            for (Class inter: interfaces) {
                String interfaceName = inter.getName();
                logger.info("加载服务类：{}", interfaceName);
                serviceMap.put(interfaceName, bean);
            }
        }
        logger.info("以加载完所有服务接口:{}", serviceMap);
    }

    private void start() {
        final  NettyServerHandler serverHandler = new NettyServerHandler(serviceMap);

        new Thread(() -> {
            try {
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 1024)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            // 创建NIOSocketChannel成功后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络IO事件
                            @Override
                            protected void initChannel(SocketChannel channel) throws Exception {
                                ChannelPipeline pipeline = channel.pipeline();
                                pipeline.addLast(new IdleStateHandler(0, 0, 60));
                                pipeline.addLast(new JSONEncoder());
                                pipeline.addLast(new JSONDecoder());
                                pipeline.addLast(serverHandler);
                            }
                        });
                String[] arr = serverAddress.split(":");
                String host = arr[0];
                int port = Integer.parseInt(arr[1]);

                ChannelFuture cf = serverBootstrap.bind(host, port).sync();

                logger.info("rpc服务端启动,监听端口: " + port);
                serviceRegistry.registry(serverAddress);

                cf.channel().closeFuture().sync();

            } catch (Exception e) {
                e.printStackTrace();

                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
}

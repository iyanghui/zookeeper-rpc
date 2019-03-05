package pers.zhixilang.rpc.consumer.connection;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pers.zhixilang.rpc.consumer.netty.client.NettyClient;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhixilang
 * @version 1.0
 * @date 2019-03-03 20:36
 */
@Component
public class ConnectionManage {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManage.class);

    @Resource
    private NettyClient nettyClient;

    private AtomicInteger roundRobin = new AtomicInteger(0);

    private CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<>();

    private Map<SocketAddress, Channel> channelNodes = new ConcurrentHashMap<>();

    public synchronized void updateConnectServer(List<String> addressList) {
        if (addressList == null || addressList.size() == 0) {
            logger.info("全部服务节点已关闭");

            for (final Channel channel: channels) {
                SocketAddress remotePeer = channel.remoteAddress();

                Channel node = channelNodes.get(remotePeer);
                node.close();
            }
            channels.clear();
            channelNodes.clear();
            return;
        }

        HashSet<SocketAddress> newAllServerNodeSet = new HashSet<>();
        for (int i = 0; i < addressList.size(); i++) {
            String[] arr = addressList.get(i).split(":");
            if (arr.length == 2) {
                String host = arr[0];
                int post = Integer.parseInt(arr[1]);
                final SocketAddress socketAddress = new InetSocketAddress(host, post);
                newAllServerNodeSet.add(socketAddress);
            }
        }

        for (final SocketAddress socketAddress: newAllServerNodeSet) {
            Channel channel = channelNodes.get(socketAddress);
            if (channel == null || !channel.isOpen()) {
                connectServerNode(socketAddress);
            }
        }



    }

    public Channel chooseChannel() {
        if (channels.size() > 0) {
            int size = channels.size();
            int index = (roundRobin.getAndAdd(1) + size) % size;
            return channels.get(index);
        } else {
            return null;
        }
    }


    public void removeChannel(Channel channel) {
        SocketAddress address = channel.remoteAddress();
        channels.remove(channel);
        channelNodes.remove(address);
        logger.info("从连接管理器中移除channel.{}", address);
    }

    private void connectServerNode(SocketAddress socketAddress) {
        try {
            Channel channel = nettyClient.doConnect(socketAddress);
            addChannel(channel, socketAddress);
        } catch (Exception e) {
            logger.error("未能连接到服务器.{}", socketAddress);
        }
    }

    private void addChannel(Channel channel, SocketAddress socketAddress) {
        channels.add(channel);
        channelNodes.put(socketAddress, channel);
        logger.info("channel{}加入中连接管理器", socketAddress);
    }
}

package net.abadguy.zk.demo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @ClassName:
 * @Description:
 * @author: liujiwei
 * @date: 2019/6/1715:05
 */
public class ZKConnect implements Watcher {

   final static Logger log = LoggerFactory.getLogger(ZKConnect.class);

   public static final String zkServerPath="127.0.0.1:2181";

   public static final Integer timeout=50000;

    public static void main(String[] args) throws IOException, InterruptedException {
        /**
         * 客户端和zk服务端连接时一个异步过程
         * 当连接成功后，客户端会受到一个watch通知
         *
         * 参数：
         * connectString:连接服务器的ip字符串
         *      比如:"192.168.1.1:2181,192.168.1.2:2181,291.168.1.3:2181"
         *      可以时一个ip，也可以是多个ip
         * sessionTimeout:超时时间，心跳收不到了，那就是超时
         * watcher:通知事件，如果有对应的事件触发，则会收到一个通知，如果不需要，就设置成null
         * canBeReadOnly:刻度，当这个物理机节点断开后，还是可以读到数据的，只是不能写入
         * sessionId:会话id
         * sessionPassswd:会话密码，当会话丢失后，可以依据sessionId和sessionPassword重新获取会话
         */
        ZooKeeper zk=new ZooKeeper(zkServerPath,timeout,new ZKConnect());

        log.debug("客户端开始连接zookeeper服务器...");
        log.debug("连接状态：{}",zk.getState());
        Thread.sleep(2000);
        log.debug("连接状态：{}",zk.getState());
    }

    public void process(WatchedEvent watchedEvent) {
        log.debug("收到watch通知：{}",watchedEvent);
    }
}

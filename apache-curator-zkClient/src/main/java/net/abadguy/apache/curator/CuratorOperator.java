package net.abadguy.apache.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * @ClassName:
 * @Description: 使用 Apache Curator客户端连接zk
 * @author: ABadguy
 * @date: 2019/6/1715:37
 */
public class CuratorOperator {

    public CuratorFramework client=null;
    public static final String zkServerPath="127.0.0.1:2181";

    /**
     * 实例化zk客户端
     */
    public CuratorOperator(){

        /**
         * curator连接zk的策略:RetryNTimes
         * n: 重试次数
         * sleepMsBetweenRetries: 每次重试时间间隔
         */
        RetryPolicy retryPolicy=new RetryNTimes(3,500);

        client= CuratorFrameworkFactory.builder().
                connectString(zkServerPath).
                sessionTimeoutMs(10000).retryPolicy(retryPolicy).
                build();
        client.start();
    }

    public void closeZkClient(){
        if(client!=null){
            this.client.close();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        //实例化
        CuratorOperator cto=new CuratorOperator();
        boolean isZkCuratorStarted=cto.client.isStarted();
        System.out.println("当前客户端的状态"+(isZkCuratorStarted ? "连接中" : "已关闭"));

        Thread.sleep(3000);

        cto.closeZkClient();
        boolean isZkCuratorStarted2=cto.client.isStarted();
        System.out.println("当前客户端的状态"+(isZkCuratorStarted2 ? "连接中" : "已关闭"));
    }
}

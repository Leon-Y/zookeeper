package com.yx.test.zookeeper.consumer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;

/**
 * @Auther: 36560
 * @Date: 2020/3/27 :8:15
 * @Description: zookeeper的worker节点
 */
public class Consumer implements Watcher {

    private static final Logger log = Logger.getLogger(Consumer.class);

    private ZooKeeper zk;
    private String hostPort;
    private Random random = new Random();
    private String serverId = Integer.toHexString(random.nextInt());
    private String status;


    Consumer(String hostPort) {
        this.hostPort = hostPort;
    }

    void startZk() throws IOException {
        zk = new ZooKeeper(hostPort, 15000, this);
    }

    void stopZk() throws InterruptedException {
        zk.close();
    }

    public void process(WatchedEvent watchedEvent) {
        log.info("监听事件："+watchedEvent);
    }

    /**
     * 创建worker回调函数
     * 1.连接丢失：connectionloss 连接丢失的情况下，重新进行注册
     * 2.注册成功 注册成功则结束
     * 3.node已注册 已注册的情况下将提示已注册，成功返回
     * 4.默认情况：未知错误内容
     */
    AsyncCallback.StringCallback creatWorkerCallBack = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    register();
                    break;
                case OK:
                    log.info("registered successfully:" + serverId);
                    break;
                case NODEEXISTS:
                    log.warn("already exsits:" + serverId);
                    break;
                default:
                    log.error("something went wrong:" + KeeperException.create(KeeperException.Code.get(rc), path));
            }

        }
    };

    /**
     * 注册worker
     */
    void register() {
        zk.create("/worker/worker-" + serverId, "Idle".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL,creatWorkerCallBack,null);
    }

    /**
     * 更新状态的回调函数
     */
    AsyncCallback.StatCallback statusUpdateCallBack = new AsyncCallback.StatCallback() {
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    updateStatus((String) ctx);
                    break;
                case OK:
                    log.info("update successfully");
                    break;
                default:
                    log.error("unknown error");
            }
        }
    };

    /**
     * 更新状态
     */
    void updateStatus(String status){
        if(this.status == status){
            zk.setData("/worker/"+serverId,status.getBytes(),-1,statusUpdateCallBack,status);
        }
    }

    /**
     * 设置状态入口
     * @param status
     */
    void setStatus(String status){
        this.status = status;
    }

    /**
     * 启动一个worker
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void start(String[] args) throws IOException, InterruptedException {
        Consumer watcher = new Consumer(args[1]);
        watcher.startZk();
        watcher.register();
        Thread.sleep(30000);
        watcher.stopZk();
    }
}


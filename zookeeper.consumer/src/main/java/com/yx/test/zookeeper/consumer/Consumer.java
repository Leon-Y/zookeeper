package com.yx.test.zookeeper.consumer;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;

/**
 * @Auther: 36560
 * @Date: 2020/3/27 :8:15
 * @Description: zookeeper的监视点
 */
public class Consumer implements Watcher {

    static ZooKeeper zk ;
    static String hostPort;
    static Random random = new Random();
    static String serverId = Integer.toHexString(random.nextInt());
    static boolean isLeader = false;
    static AsyncCallback.StringCallback masterStrinCallBack = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    checkMaster();
                    return;
                case OK:
                    isLeader = true;
                    break;
                default:
                    isLeader =false;
            }
            System.out.println("I am "+(isLeader?"":"not")+" the leader");
        }
    };

    static AsyncCallback.DataCallback masterCheckCallBack = new AsyncCallback.DataCallback() {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    checkMaster();
                    return;
                case NONODE:
                    runForMaster();
                    return;

            }
        }
    };
    /**
     * 检查是否存在master
     * @return
     */
    static void checkMaster() {
        zk.getData("/master", false, masterCheckCallBack,null);
    }

    public Consumer(String hostPort) {
        this.hostPort = hostPort;
    }

    void startZk() throws IOException {
        zk = new ZooKeeper(hostPort,15000,this);
    }

    void stopZk() throws InterruptedException {
        zk.close();
    }
    public void process(WatchedEvent watchedEvent) {
//        log.info("监听事件");
        System.out.println(watchedEvent);
    }

    /**
     *  尝试获取master节点
     */
    static void  runForMaster(){
        zk.create("/master",serverId.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL,masterStrinCallBack,null);
    }

    public void bootstrap(){
        creatParent("/workers",new byte[0]);
        creatParent("/assign",new byte[0]);
        creatParent("/tasks",new byte[0]);
        creatParent("/status",new byte[0]);
    }

    void creatParent(String path,byte[] data){
        zk.create(path,data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT,createParentCallback,null);
    }

    AsyncCallback.StringCallback createParentCallback = new AsyncCallback.StringCallback() {
        public void processResult(int rc, String path, Object ctx, String name) {
            switch (KeeperException.Code.get(rc)){
                case CONNECTIONLOSS:
                    creatParent(path,(byte[])ctx);
                case OK:
                    System.out.println("parent created");
                    break;
                case NODEEXISTS:
                    System.out.println("parent exist:"+path);
                    break;
                    default:
                        System.out.println("something was wrong:"+KeeperException.create(KeeperException.Code.get(rc),path));
            }
        }
    };
    public static void start(String[] args) throws IOException, InterruptedException {
        Consumer watcher = new Consumer(args[0]);
        watcher.startZk();
        watcher.runForMaster();
        if (watcher.isLeader){
            System.out.println("I am a leader");
        }else {
            System.out.println("some else is the leader");
        }
        while (true){
            System.out.println(System.currentTimeMillis());
            Thread.sleep(60000);
        }
//        watcher.stopZk();
    }
}


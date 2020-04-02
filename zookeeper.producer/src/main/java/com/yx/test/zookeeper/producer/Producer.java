package com.yx.test.zookeeper.producer;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * @Auther: 36560
 * @Date: 2020/3/27 :8:15
 * @Description: zookeeper的监视点
 */
public class Producer implements Watcher {

    private static final Logger log = Logger.getLogger(Producer.class);

    private ZooKeeper zk ;
    private String hostPort;

    public Producer(String hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * 开始zk
     * @throws IOException
     */
    void startZk() throws IOException {
        zk = new ZooKeeper(hostPort,15000,this);
    }

    /**
     * 生产有序的任务
     * @param command
     * @return
     * @throws InterruptedException
     */
    String queueCommand(String command) throws InterruptedException {
        while (true){
            try {
                //PERSISTENT_SEQUENTIAL持久的有序节点
                String name = zk.create("/task/task-", command.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
                return name;
            } catch (KeeperException.NodeExistsException e) {
                log.warn("node exists:"+command);
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止zk
     * @throws InterruptedException
     */
    void stopZk() throws InterruptedException {
        zk.close();
    }

    /**
     * 监听事件启动的日志
     * @param watchedEvent
     */
    public void process(WatchedEvent watchedEvent) {
        log.info("producer:"+watchedEvent);
    }


    /**
     * 开始一个事件的创建者
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void start(String[] args) throws IOException, InterruptedException {
        Producer watcher = new Producer(args[1]);
        watcher.startZk();
        String name = watcher.queueCommand(args[2]);
        log.info("creat:"+name);
//          watcher.stopZk();
    }
}


package com.yx.test.zookeeper.client;

import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Date;

/**
 * @Auther: 36560
 * @Date: 2020/4/2 :21:57
 * @Description:
 */
public class AdminClient implements Watcher {
    private static final Logger log = Logger.getLogger(AdminClient.class);
    private ZooKeeper zk;
    private String hostPort;

    public AdminClient(String hostPort) {
        this.hostPort = hostPort;
    }

    public void start() throws IOException {
        this.zk = new ZooKeeper(hostPort,15000,this);
    }

    public void listState() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        try {
            byte[] data = zk.getData("/master", false, stat);
            Date date = new Date(stat.getCtime());
            log.info("master:"+new String(data)+"since "+date);
        } catch (KeeperException.NoNodeException e) {
            log.error("no master");
        }
        log.info("workers:");
        try {
            for (String w:zk.getChildren("/workers",false)){
                byte[] data = new byte[0];
                try {
                    data = zk.getData("/workers/" + w, false, null);
                } catch (KeeperException.NoNodeException e) {
                    log.error("no worker");
                }
                String state = new String(data);
                log.info("\t"+w+":"+state);
            }
        } catch (KeeperException.NoNodeException e) {
            log.error("no workers");
        }
        log.info("tasks:");
        try {
            for (String t: zk.getChildren("/assign",false)){
                log.info("\t"+t);
            }
        } catch (KeeperException.NoNodeException e) {
            log.info("no assign");
        }
    }
    public void process(WatchedEvent event) {
        log.info(event);
    }

    public static void start(String[] args) throws IOException, KeeperException, InterruptedException {
        AdminClient adminClient = new AdminClient(args[1]);
        adminClient.start();
        while (true){
            adminClient.listState();
            Thread.sleep(30000);
        }
    }
}

package com.yx.test.zookeeper.bootstrap;


import com.yx.test.zookeeper.admin.Master;
import com.yx.test.zookeeper.consumer.Consumer;
import com.yx.test.zookeeper.producer.Producer;
import com.yx.test.zookeeper.client.*;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @Auther: 36560
 * @Date: 2020/4/2 :6:45
 * @Description:
 */
public class Main {
    private static final Logger log =Logger.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        if (args.length == 0){
            log.error("请输入启动参数");
            throw new RuntimeException("请输入启动参数");
        }
        String arg = args[0];
        if(arg.equals("master")){
            log.info("启动主节点");
            Master.start(args);
        }else if (arg.equals("producer")){
            log.info("启动生产者");
            Producer.start(args);
        }else if (arg.equals("consumer")){
            log.info("启动消费者");
            Consumer.start(args);
        }else if (arg.equals("client")){
            log.info("启动客户端观察者");
            AdminClient.start(args);
        }else {
            log.error("参数错误");
        }

    }
}

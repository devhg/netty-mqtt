package cn.sdutcs.mqtt.broker.handler;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

public class TrafficHandler {

    // private static final EventExecutorGroup EXECUTOR_GROUP = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
    //
    // public static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(EXECUTOR_GROUP, 30, 30);
    //
    // static {
    //     new Thread(new Runnable() {
    //         @Override
    //         public void run() {
    //             while (true) {
    //                 TrafficCounter trafficCounter = trafficHandler.trafficCounter();
    //                 try {
    //                     TimeUnit.SECONDS.sleep(1);
    //                 } catch (InterruptedException e) {
    //                     e.printStackTrace();
    //                 }
    //                 final long totalRead = trafficCounter.cumulativeReadBytes();
    //                 final long totalWrite = trafficCounter.cumulativeWrittenBytes();
    //                 System.out.println("total read: " + (totalRead >> 10) + " KB");
    //                 System.out.println("total write: " + (totalWrite >> 10) + " KB");
    //                 System.out.println("流量监控: " + System.lineSeparator() + trafficCounter);
    //             }
    //         }
    //     }).start();
    // }
}

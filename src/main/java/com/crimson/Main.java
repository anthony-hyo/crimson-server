package com.crimson;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class Main {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class);

    public static final GlobalTrafficShapingHandler GLOBAL_TRAFFIC_SHAPING_HANDLER = new GlobalTrafficShapingHandler(Executors.newScheduledThreadPool(1), 1000);

    public static void main(String[] args) {
        log.info("#################### Logger");
        setupLogger();
    }
    private static void setupLogger() {
    }

}
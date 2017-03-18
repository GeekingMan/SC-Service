package com.geek.sc.rabbitmq;
import org.springframework.cloud.stream.binder.ConsumerProperties;

public class ExtendConsumerProperties extends ConsumerProperties {
    private boolean routing = false;

    public boolean isRouting() {
        return routing;
    }

    public void setRouting(boolean routing) {
        this.routing = routing;
    }
}
package com.geek.sc.rabbitmq.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface InputChannel {
    String inputChannel = "inputChannel";

    @Input(inputChannel)
    SubscribableChannel input();
}

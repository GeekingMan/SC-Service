package com.geek.sc.rabbitmq.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface OutputChannel {
    String outputChannel = "outputChannel";

    @Input(outputChannel)
    MessageChannel output();
}

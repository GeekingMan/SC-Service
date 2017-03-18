package com.geek.sc.rabbitmq;

/**
 * Created by Assassin on 2017/3/19.
 */
public enum StreamRoute {
    SEND_KEY("send_key"), RECEIVE_KEY("receive_key");

    private String value;

    StreamRoute(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

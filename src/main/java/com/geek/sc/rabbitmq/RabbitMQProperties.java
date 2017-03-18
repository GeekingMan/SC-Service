package com.geek.sc.rabbitmq;

import java.util.Map;

public class RabbitMQProperties {
    private Map<String, BindingProperties> bindings;

    public Map<String, BindingProperties> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, BindingProperties> bindings) {
        this.bindings = bindings;
    }
}
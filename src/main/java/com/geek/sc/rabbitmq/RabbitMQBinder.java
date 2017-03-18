package com.geek.sc.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.postprocessor.DelegatingDecompressingPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.DefaultBinding;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.rabbit.RabbitConsumerProperties;
import org.springframework.cloud.stream.binder.rabbit.RabbitMessageChannelBinder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.support.DefaultAmqpHeaderMapper;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RabbitMQBinder extends RabbitMessageChannelBinder {
    private static final String DEAD_LETTER_EXCHANGE = "DLX";
    private ConnectionFactory connectionFactory;
    private MessagePostProcessor decompressingPostProcessor = new DelegatingDecompressingPostProcessor();
    private static final MessagePropertiesConverter inboundMessagePropertiesConverter =
            new DefaultMessagePropertiesConverter() {
                @Override
                public MessageProperties toMessageProperties(AMQP.BasicProperties source, Envelope envelope,
                                                             String charset) {
                    MessageProperties properties = super.toMessageProperties(source, envelope, charset);
                    properties.setDeliveryMode(null);
                    return properties;
                }
            };

    @Autowired
    private RabbitMQProperties rabbitMQProperties;

    public RabbitMQBinder(ConnectionFactory connectionFactory, RabbitProperties rabbitProperties) {
        super(connectionFactory, rabbitProperties);
        this.connectionFactory = connectionFactory;
    }

    @Override
    protected String buildPartitionRoutingExpression(String expressionRoot) {
        return "'" + expressionRoot + ".' + headers['" + StreamRoute.SEND_KEY + "']";
    }

    public Binding<MessageChannel> doBindConsumer(String name, String group, MessageChannel inputChannel,
                                                  ExtendedConsumerProperties<RabbitConsumerProperties> properties) {
        boolean anonymousConsumer = !StringUtils.hasText(group);
        String baseQueueName = anonymousConsumer ? groupedName(name, ANONYMOUS_GROUP_NAME_GENERATOR.generateName())
                : groupedName(name, group);
        if (this.logger.isInfoEnabled()) {
            this.logger.info("declaring queue for inbound: " + baseQueueName + ", bound to: " + name);
        }
        String prefix = properties.getExtension().getPrefix();
        String exchangeName = applyPrefix(prefix, name);
        TopicExchange exchange = new TopicExchange(exchangeName);
        declareExchange(exchangeName, exchange);

        String queueName = applyPrefix(prefix, baseQueueName);
        boolean partitioned = !anonymousConsumer && properties.isPartitioned();
        boolean durable = !anonymousConsumer && properties.getExtension().isDurableSubscription();
        Queue queue;

        if (anonymousConsumer) {
            queue = new Queue(queueName, false, true, true);
        } else {
            if (partitioned) {
                String partitionSuffix = "-" + properties.getInstanceIndex();
                queueName += partitionSuffix;
            }
            if (durable) {
                queue = new Queue(queueName, true, false, false,
                        queueArgs(queueName,
                                properties.getExtension().getPrefix(),
                                properties.getExtension().isAutoBindDlq()));
            } else {
                queue = new Queue(queueName, false, false, true);
            }
        }

        declareQueue(queueName, queue);

        if (partitioned) {
            String bindingKey = String.format("%s-%d", name, properties.getInstanceIndex());
            declareBinding(queue.getName(), BindingBuilder.bind(queue).to(exchange).with(bindingKey));
        } else {
            if (inputChannel instanceof AbstractMessageChannel) {
                String channelName = ((AbstractMessageChannel) inputChannel).getComponentName();
                if (rabbitMQProperties.getBindings().get(channelName) != null
                        && rabbitMQProperties.getBindings().get(channelName).getConsumer() != null
                        && rabbitMQProperties.getBindings().get(channelName).getConsumer().isRouting()) {
                    declareBinding(queue.getName(), BindingBuilder.bind(queue).to(exchange).with(queueName));
                } else {
                    declareBinding(queue.getName(), BindingBuilder.bind(queue).to(exchange).with("#"));
                }
            } else {
                throw new RuntimeException("The type of MessageChannel is not be supported");
            }
        }
        Binding<MessageChannel> binding = doRegisterConsumer(baseQueueName, group, inputChannel, queue, properties);
        if (durable) {
            autoBindDLQ(applyPrefix(prefix, baseQueueName), queueName,
                    properties.getExtension().getPrefix(), properties.getExtension().isAutoBindDlq());
        }
        return binding;
    }

    private void autoBindDLQ(final String queueName, String routingKey, String prefix, boolean autoBindDlq) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("autoBindDLQ=" + autoBindDlq + " for: " + queueName);
        }
        if (autoBindDlq) {
            String dlqName = constructDLQName(queueName);
            Queue dlq = new Queue(dlqName);
            declareQueue(dlqName, dlq);
            final String dlxName = deadLetterExchangeName(prefix);
            final DirectExchange dlx = new DirectExchange(dlxName);
            declareExchange(dlxName, dlx);
            declareBinding(dlqName, BindingBuilder.bind(dlq).to(dlx).with(routingKey));
        }
    }

    private Map<String, Object> queueArgs(String queueName, String prefix, boolean bindDlq) {
        Map<String, Object> args = new HashMap<>();
        if (bindDlq) {
            args.put("x-dead-letter-exchange", applyPrefix(prefix, "DLX"));
            args.put("x-dead-letter-routing-key", queueName);
        }
        return args;
    }

    private Binding<MessageChannel> doRegisterConsumer(
            final String name, String group, MessageChannel moduleInputChannel, Queue queue,
            final ExtendedConsumerProperties<RabbitConsumerProperties> properties) {
        DefaultBinding<MessageChannel> consumerBinding;
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer(
                this.connectionFactory);
        listenerContainer.setAcknowledgeMode(properties.getExtension().getAcknowledgeMode());
        listenerContainer.setChannelTransacted(properties.getExtension().isTransacted());
        listenerContainer.setDefaultRequeueRejected(properties.getExtension().isRequeueRejected());
        int concurrency = properties.getConcurrency();
        concurrency = concurrency > 0 ? concurrency : 1;
        listenerContainer.setConcurrentConsumers(concurrency);
        int maxConcurrency = properties.getExtension().getMaxConcurrency();
        if (maxConcurrency > concurrency) {
            listenerContainer.setMaxConcurrentConsumers(maxConcurrency);
        }
        listenerContainer.setPrefetchCount(properties.getExtension().getPrefetch());
        listenerContainer.setRecoveryInterval(properties.getExtension().getRecoveryInterval());
        listenerContainer.setTxSize(properties.getExtension().getTxSize());
        listenerContainer.setTaskExecutor(new SimpleAsyncTaskExecutor(queue.getName() + "-"));
        listenerContainer.setQueues(queue);
        int maxAttempts = properties.getMaxAttempts();
        if (maxAttempts > 1 || properties.getExtension().isRepublishToDlq()) {
            RetryOperationsInterceptor retryInterceptor = RetryInterceptorBuilder.stateless()
                    .maxAttempts(maxAttempts)
                    .backOffOptions(properties.getBackOffInitialInterval(),
                            properties.getBackOffMultiplier(),
                            properties.getBackOffMaxInterval())
                    .recoverer(determineRecoverer(name, properties.getExtension().getPrefix(),
                            properties.getExtension().isRepublishToDlq()))
                    .build();
            listenerContainer.setAdviceChain(new Advice[]{retryInterceptor});
        }
        listenerContainer.setAfterReceivePostProcessors(this.decompressingPostProcessor);
        listenerContainer.setMessagePropertiesConverter(RabbitMQBinder.inboundMessagePropertiesConverter);
        listenerContainer.afterPropertiesSet();
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(listenerContainer);
        adapter.setBeanFactory(this.getBeanFactory());
        DirectChannel bridgeToModuleChannel = new DirectChannel();
        bridgeToModuleChannel.setBeanFactory(this.getBeanFactory());
        bridgeToModuleChannel.setBeanName(name + ".bridge");
        adapter.setOutputChannel(bridgeToModuleChannel);
        adapter.setBeanName("inbound." + name);
        DefaultAmqpHeaderMapper mapper = new DefaultAmqpHeaderMapper();
        mapper.setRequestHeaderNames(properties.getExtension().getRequestHeaderPatterns());
        mapper.setReplyHeaderNames(properties.getExtension().getReplyHeaderPatterns());
        adapter.setHeaderMapper(mapper);
        adapter.afterPropertiesSet();
        consumerBinding = new DefaultBinding<MessageChannel>(name, group, moduleInputChannel, adapter) {
            @Override
            protected void afterUnbind() {
                cleanAutoDeclareContext(properties.getExtension().getPrefix(), name);
            }
        };
        ReceivingHandler convertingBridge = new ReceivingHandler();
        convertingBridge.setOutputChannel(moduleInputChannel);
        convertingBridge.setBeanName(name + ".convert.bridge");
        convertingBridge.afterPropertiesSet();
        bridgeToModuleChannel.subscribe(convertingBridge);
        adapter.start();
        return consumerBinding;
    }

    private final class ReceivingHandler extends AbstractReplyProducingMessageHandler {

        private ReceivingHandler() {
            super();
            this.setBeanFactory(RabbitMQBinder.this.getBeanFactory());
        }

        @Override
        protected Object handleRequestMessage(org.springframework.messaging.Message<?> requestMessage) {
            return deserializePayloadIfNecessary(requestMessage).toMessage(getMessageBuilderFactory());
        }

        @Override
        protected boolean shouldCopyRequestHeaders() {
            /*
             * we've already copied the headers so no need for the ARPMH to do it, and we don't want
             * the content-type restored if absent.
             */
            return false;
        }

    }

    private String deadLetterExchangeName(String prefix) {
        return prefix + DEAD_LETTER_EXCHANGE;
    }

    private MessageRecoverer determineRecoverer(String name, String prefix, boolean republish) {
        if (republish) {
            RabbitTemplate errorTemplate = new RabbitTemplate(this.connectionFactory);
            RepublishMessageRecoverer republishMessageRecoverer = new RepublishMessageRecoverer(errorTemplate,
                    deadLetterExchangeName(prefix),
                    applyPrefix(prefix, name));
            return republishMessageRecoverer;
        } else {
            return new RejectAndDontRequeueRecoverer();
        }
    }
}

package com.coolmq.amqp.config;

import com.coolmq.amqp.listener.DeadLetterMessageListener;
import com.coolmq.amqp.util.MQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;


/**
 * <p><b>Description:</b> RabbitMQ交换机、队列的配置类.定义交换机、key、queue并做好绑定。
 * 同时定义每个队列的ttl，队列最大长度，Qos等等
 * 这里只绑定了死信队列。建议每个队列定义自己的QueueConfig
 * <p><b>Company:</b>
 *
 * @author created by fw at 21:54 on 2017-12-23
 * @version V0.1
 */
@Configuration
public class DeadQueueConfig {


    //========================== 声明交换机 ==========================
    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(MQConstants.DLX_EXCHANGE);
    }



    //========================== 声明队列 ===========================
    /**
     * 死信队列
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(MQConstants.DLX_QUEUE,true,false,false);
    }
    /**
     * 通过死信路由key绑定死信交换机和死信队列
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange())
                .with(MQConstants.DLX_ROUTING_KEY);
    }

    /**
     * 死信队列的监听
     * @param connectionFactory RabbitMQ连接工厂
     * @param deadLetterMessageListener  死信队列监听
     * @return 监听容器对象
     */
    @Bean
    public SimpleMessageListenerContainer deadLetterListenerContainer(ConnectionFactory connectionFactory,
    		DeadLetterMessageListener deadLetterMessageListener) {

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(dlxQueue());
        container.setExposeListenerChannel(true);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(deadLetterMessageListener);
        /** 设置消费者能处理消息的最大个数 */
        container.setPrefetchCount(100);
        return container;
    }

    //====================== 一个例子，用来说明如何声明队列与交换机绑定 =======================

}

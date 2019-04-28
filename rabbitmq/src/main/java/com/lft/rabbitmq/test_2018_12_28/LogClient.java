package com.lft.rabbitmq.test_2018_12_28;

import com.lft.rabbitmq.test_2018_12_14.Client;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 消费者
 * @author Ryze
 * @date 2018-12-14 18:44
 */
public class LogClient {

    public static void main(String[] args) throws IOException, TimeoutException {
        Channel channel = Client.getConnection().createChannel();
        //创建 exchange
        channel.exchangeDeclare("amq.rabbitmq.log", "topic", true, false, true, null);
        //声明三个日志等级的消息队列，队列名随便取
        channel.queueDeclare("info_queue", false, false, false, null);
        channel.queueDeclare("error_queue", false, false, false, null);
        channel.queueDeclare("warning_queue", false, false, false, null);
        //将队列绑定到交换器上（RabbitMQ把日志发送到这个交换器上，并且是以日志等级为route_key:error、warning、info）
        channel.queueBind("info_queue", "amq.rabbitmq.log", "info");
        channel.queueBind("error_queue", "amq.rabbitmq.log", "error");
        channel.queueBind("warning_queue", "amq.rabbitmq.log", "warning");
        //创建消费者，并设计消息处理逻辑（这里仅做打印）
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("-----------MQ LOG-------------\n" + message + "\n\n");
                channel.basicAck(envelope.getDeliveryTag(), false);
            }

        };
        //消费
        channel.basicConsume("info_queue", true, consumer);
        channel.basicConsume("error_queue", true, consumer);
        channel.basicConsume("warning_queue", true, consumer);

    }

}

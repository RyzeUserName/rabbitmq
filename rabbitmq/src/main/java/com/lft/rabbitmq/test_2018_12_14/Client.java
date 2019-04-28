package com.lft.rabbitmq.test_2018_12_14;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 消费者
 * @author Ryze
 * @date 2018-12-14 18:44
 */
public class Client {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        //声明要关注的队列
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        System.out.println("Customer Waiting Received messages");
        //回调
        DeliverCallback callback=(s, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Customer Received '" + message + "'");
        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        channel.basicConsume(QUEUE_NAME, true, callback,s -> {});
    }

    public static Connection getConnection() throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址
        factory.setHost("192.168.42.128");
        factory.setUsername("admin");
        factory.setPassword("123456");
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        return connection;
    }
}

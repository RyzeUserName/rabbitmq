package com.lft.rabbitmq.test_2018_12_28;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者
 * @author Ryze
 * @date 2018-12-14 18:43
 */
public class Server {

    public final static String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        String message = "Hello RabbitMQ ...........";
        //1.获取链接
        Connection connection = Client.getConnection();
        //2.创建一个通道
        Channel channel = connection.createChannel();
        //3.发送消息到队列中 dircet 类型交换器 自定绑定到默认的交换器（默认交换器+临时队列= rpc 消息通信模式）
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println("Producer Send +'" + message + "'");
        //5.关闭通道和连接
        channel.close();
        connection.close();
    }
}

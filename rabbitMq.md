# RabbitMq

MQ全称为Message Queue，消息队列（MQ）是一种应用程序对应用程序的通信方法。应用程序通过读写出入队列的消息（针对应用程序的数据）来通信，而无需专用连接来链接它们。

生产者->有效负载+标签-> rabbitmq -> 消费者（不知道标签等，只有有效负载）

消息传递指的是程序之间通过在消息中发送数据进行通信，而不是通过直接调用彼此来通信，直接调用通常是用于诸如[远程过程调用]的技术。排队指的是应用程序通过 队列来通信。队列的使用除去了接收和发送应用程序同时执行的要求

使用Mnesia存储队列 交换器 绑定信息等

**JMS**是SUN JAVA 消息中间件 服务的一个标准和API定义，试图通过java api屏蔽不同的消息组件的差异（但是不同程序走的不同的驱动，导致并未最终解决问题），而MQ则是遵循了AMQP协议的具体实现和产品

**RabbitMQ**是一个在 AMQP基础上完成的，可复用的企业消息系统( Erlang 语言)

通信就是在 tcp的一条连接 有多条的amqp 信道（用于接收和传输，每个信道都会指派唯一的ID（AMQP会记住）

生产者->交换器-> 绑定 ->对应的队列->消费者

AMQP消息路由 = 交换器+ 队列+ 绑定

![](.\assets\1.jpg)



**队列**

消费者消费的类型**：

​	1.basic.consume订阅

​	2.basic.get 获取单条 而不是订阅

当存在多个消费者，订阅一个队列，那么会是round-robin 分发。（**注意：**消费者收到消息后，需要确认收到，发送

basic.ack 给 rabbitmq或者 在订阅的时候 将auto_ack =true 默认确认，未确认的消息 会被认为消息没有消费，发送给下一

个消费者，并且若该消费者忘了确认消息的话，rabbit 不会给该消费者发送 更多消息，会认为你没有准备好处理下一条消

息 ，当然也可以拒绝(未确认之前) 1.断开 rabbit 的连接 2. basic.reject [ 参数 requeue 为true rabbit 立马将消息重新发送

给下一个消费者，为false 将消息移除  ] ）

**创建队列/绑定队列：**queue.declare 指定名称 不指定 会随机分配存在的名称 

​	队列设置的参数： exclusive =true 队列变成私有的，此时只有你的应用程序才能够消费队列消息

​				      auto-delete 当最后一个消费者取消订阅，队列自动移除	

**注意 ：**当队列存在，声明参数完全匹配现在的队列的话，返回成功，参数不匹配返回错误，如果你想检测队列是否存在，

可以设置queue.declare的 passive 为true,队列存在那么成功，否则返回失败。

**队列应该由谁创建？**生产者消费者都尝试，或者消费者创建（自己有消息丢失的处理策略）



**交换器**

**消息发送给交换器，然后根据确定的规则将消息分派到哪个队列，这些规则就叫做路由键**	

**交换器 类型**：headers 交换器允许匹配消息的header 而非路由键除此之外并无特别，一般不用

​			direct（直接交换器）  服务器必须实现交换器 名称为 “” 的 默认交换器， 根据队列名称作为路由键匹配  							（默认交换器+临时队列= rpc 消息通信模式）(一对一)

​			fanout（广播交换器） 交换器  将接收到的消息广播到绑定的队列上，将接收到的消息投递到绑定到其上的					绑定的队列(一对多)

​			topic 交换器 它使得不同来源的消息到同一队列（比如：日志对列）（多对一）

**多租户模式**：虚拟主机（vhost）和隔离 (各个虚拟主机之间 隔离)，拥有独立的权限机制，默认是 vhost: "/"

​			AMQP并没有指定权限的实现是在Vhost级别还是服务器端 （rabbitmq 是在Vhost 级别实现） 

**持久化**： 持久化模式（投递模式选项设置为2）+持久化的交换器+持久化的队列

​		**注意**：持久化 效率低下	（限度 ：每秒处理100000 消息 ）

**AMQP事务**:  生产者投递消息到下消息发放到队列 实质上是一个事务

​		当把信道设置成事务模式，通过信道发送那些需要确认的消息，之后还有很多AMQP命令,那么这些命令是否执

​		行取决于第一条是否执行成功，成功那么执行之后的命令，否则。。但是性能很差，几乎吸干了rabbit的性能。

​		而且 也将消息变成了同步的。所有-》 发送方确认模式，将信道设置成confirm 模式，每个消息均会有个发送的

​		唯一ID只有服务器端分发到队列（持久化到磁盘） 会发送一个确认的ID（回调），让客户端知晓。

## 1.安装

百度基本可以，注意 远程连接的需要创建远程连接账号和配置，很恶心

## 2.Hello World

```java
/**
 * 生产者
 * @author Ryze
 * @date 2018-12-14 18:43
 */
public class Server {

    public final static String QUEUE_NAME = "rabbitMQ.test";

    public static void main(String[] args) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("192.168.42.128");
        //创建一个新的连接
        Connection connection = factory.newConnection();
        factory.setUsername("admin");
        factory.setPassword("123456");
        //创建一个通道
        Channel channel = connection.createChannel();
        // 声明一个消息
        String message = "Hello RabbitMQ";
        //发送消息到队列中
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println("Producer Send +'" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }
}
/**
 * 消费者
 * @author Ryze
 * @date 2018-12-14 18:44
 */
public class Client {
    private final static String QUEUE_NAME = "rabbitMQ.test";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址
        factory.setHost("192.168.42.128");
        factory.setUsername("admin");
        factory.setPassword("123456");
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //声明要关注的队列
        channel.queueDeclare(QUEUE_NAME, false, false, true, null);
        System.out.println("Customer Waiting Received messages");
        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Customer Received '" + message + "'");
            }
        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
```

## 3.运行管理

​	Erlang节点，Erlang虚拟机的一个实例，一个Erlang运行着一个Erlang应用程序，节点之间可以进行本地通信（不论是否在同一台服务器上），同时应用程序崩溃，Erlang节点自动尝试重启应用程序（前提是Erlang没有崩溃）

​	以下目录均指，安装的rabbitmq 目录下的相对目录

​	**日志**在   var/log/rabbitmq/rabbit@[hostname].log

​	**启动**    sbin 目录下 ./rabbitmq-server    后台启动 ./rabbitmq-server -detached

​	**停止**   sbin 目录下 ./rabbitmqctl stop [- n rabbit@[hostname ] ] 指定节点停止

​	**停止RabbitMQ**  sbin 目录下 ./rabbitmqctl stop_app 不会关闭Erlang

​	**配置文件**在 etc/rabbitmq/rabbitmq.config 是Erlang的结构 [  {选项名，[ {option_name,option_value} ]}, ...  ].  

​	**权限管理**  sbin 目录下

​	**创建用户** ./rabbitmqctl  add_user username password

​	**删除用户**  ./rabbitmqctl  delete_user username 

​	**查看用户** ./rabbitmqctl  list_users

​	**修改密码** ./rabbitmqctl  change_password  username 

​	**权限相关**： 读  消费消息    写：发布消息     配置：队列交换器的创建和删除

​	权限命令= 被授权的用户+访问控制应用的vhost+权限+权限范围（客户端/服务器的 队列/交换器）

​	 ./rabbitmqctl  set_permissions -p   vhost  username  ". *"  ". *"   ". *"    配置  写 读 全部权限

​	./rabbitmqctl  list_permissions -p vhost  查看 vhost 权限配置

​	./rabbitmqctl  list_user_permissions  username 查看 username 权限配置

​	./rabbitmqctl clear_permissions -p vhost username 移除 username  在 vhost 上的权限

​	./rabbitmqctl  -p  指明虚拟机或者路径  不写 默认/

​	./rabbitmqctl  list_queues 输出服务器已经声明的队列和消息总数（可以 加上  name（名称） messages （信息）

​	consumers （消费） memory（内存 ） 参数 显示更多信息）

​	./rabbitmqctl  list_exchanges 输出服务器交换器名称和类型（可以加上很多信息 同上 显示更多信息）

​	./rabbitmqctl  list_bindings 绑定信息(不接收  -p 参数) 返回 交换器名称 队列名称 路由键和参数

​	日志分为两部分 sasl 和普通日志  sasl  是一个标准开发库（Erlang的）

​	轮换日志： ./rabbitmqctl  rotate_logs suffix   suffix  通常是个数字 添加到轮换日志的末尾 创建新的日志

​	AMQP 有实时日志 在amq.rabbitmq.log的topic 交换器

## 4.
package test;

import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RPCServer {
	public static void main (String args[]) throws Exception{
		String queueName = "console_queue";
		String id;
	ConnectionFactory factory = new ConnectionFactory();
	factory.setHost("106.3.38.50");
	factory.setUsername("yuanshuju");
	factory.setPassword("123Yuanshuju456");

	Connection connection = factory.newConnection();
	Channel channel = connection.createChannel();

	channel.queueDeclare(queueName, false, false, false, null);

	channel.basicQos(1);

	QueueingConsumer consumer = new QueueingConsumer(channel);
	channel.basicConsume(queueName, false, consumer);

	System.out.println(" [x] Awaiting RPC requests");
	

	while (true) {
	    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

	    BasicProperties props = delivery.getProperties();
	    String message = new String(delivery.getBody());
	    ConsoleCommandBean commandBean = JSONObject.parseObject(message, ConsoleCommandBean.class);
	    id = commandBean.getId();
	    System.out.println("In RPCServer: id got from commandBean is " + id);
	    System.out.println("In RPCServer: id got from property is " + props.getCorrelationId());
	    BasicProperties replyProps = new BasicProperties
	                                     .Builder()
	                                     .correlationId(id/*props.getCorrelationId()*/)
	                                     .build();

	    

	    System.out.println("message received: " + message);
	    String response = message;
	    
	    channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes());
	    System.out.println("ReplyQueueName: " + props.getReplyTo());

	    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	}
	}

}

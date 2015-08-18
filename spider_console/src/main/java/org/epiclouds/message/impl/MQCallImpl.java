package org.epiclouds.message.impl;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epiclouds.message.abstracts.AbstractMessageCall;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * the mq implementation
 * @author xianglong
 * @created 2015-06-25 11:23:09
 * @version 1.0
 */

public class MQCallImpl extends AbstractMessageCall{
	private static Logger mainlogger = LoggerFactory.getLogger(MQCallImpl.class);
	
	private ConnectionFactory factory=null;
	
	
	private Connection connection = null;
	
	
	
	public MQCallImpl() throws IOException{
		if(factory==null){
			factory = new ConnectionFactory();
		}
	}
	
	
	
	public void setConnection() throws IOException{
		factory.setHost(ConsoleConfig.getRabbit_host());
		factory.setUsername(ConsoleConfig.getRabbit_username());
		factory.setPassword(ConsoleConfig.getRabbit_password());
		factory.setAutomaticRecoveryEnabled(false);
		connection=factory.newConnection();
	}
	
	@Override
	public String getMessage(String... sources) {
		 Channel channel = null;
		 QueueingConsumer consumer = null;
		 String queueName = sources[0];
		 String message = null;
		 try{
			 if(connection==null){
					connection=factory.newConnection();
			 }
			channel = connection.createChannel();
			channel.queueDeclare(queueName, false, false, false, null);
			consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, false, consumer);  
			/*noAck = false，需要回复，接收到消息后，queue上的消息不会被清除，
			 * 直到调用channel.basicAck(deliveryTag, false); queue上的消息才会被清除 
			 * 而且，在当前连接断开以前，其它客户端将不能收到此queue上的消息*/
		    
		    while (true) {
	        	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	        	message = new String(delivery.getBody(),"utf-8");
	        	if (message != null) {
	        		channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
	        		break;
	        	}
	        }
		    		    
		 }catch(Exception e){
			 mainlogger.error(e.getLocalizedMessage(),e);
			 if(e instanceof com.rabbitmq.client.AlreadyClosedException||!connection.isOpen()){
				 try{
					 connection.close();
				 }catch(Exception e2){}
				 try {
					connection=factory.newConnection();
				} catch (IOException e1) {
					 mainlogger.error(e1.getLocalizedMessage(),e1);
				}
			 }
			 try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
		 }finally{
			 if(channel!=null){
				 try {
					channel.close();
				} catch (IOException e) {
				}
			 }
		 }
		return message;	   
	}

	@Override
	public void sendMessage(String destination, Object... others) {
		Channel channel = null;
		String corrId = UUID.randomUUID().toString();
	    try{
	    	if(connection==null){
				connection=factory.newConnection();
			}
		    channel = connection.createChannel();
		    channel.queueDeclare(destination, false, false, false, null);  
		    
		    BasicProperties props = new BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .build(); 
		    ConsoleCommandBean temp = (ConsoleCommandBean)others[0];
		    temp.setId(corrId);
	        channel.basicPublish("", destination, props, JSONObject.toJSONString(temp).getBytes()); 
	    }catch(Exception e){
	    	mainlogger.error(e.toString(),e);
	    	if(e instanceof com.rabbitmq.client.AlreadyClosedException||!connection.isOpen()){
				 try{
					 connection.close();
				 }catch(Exception e2){}
				 try {
					connection=factory.newConnection();
				} catch (IOException e1) {
					 mainlogger.error(e1.getLocalizedMessage(),e1);
				}
			 }
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
	    }finally{
	    	if(channel!=null){
	    		try {
					channel.close();
				} catch (IOException e) {
				}
	    	}
	    	
	    }
	}
	
	public String sendAndReceive (String destination, Object... others) {
		Channel channel = null;
		String replyQueueName;
		String corrId = UUID.randomUUID().toString();
		long timeOut = 30 * 1000;
		String response = null;
	    try{
	    	if(connection==null){
				connection=factory.newConnection();
			}

	        channel=connection.createChannel();
		    replyQueueName = destination + "_result";
		    channel.queueDeclare(destination, false, false, false, null);  
//		    consumer = new QueueingConsumer(channel); 
//		    channel.basicConsume(replyQueueName, true, consumer);
		    
		    BasicProperties props = new BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .build(); 
		    
		    ConsoleCommandBean temp = (ConsoleCommandBean)others[0];
		    temp.setId(corrId);
	        channel.basicPublish("", destination, props, JSONObject.toJSONString(temp).getBytes()); 
		 
	        channel.queueDeclare(replyQueueName, false, false, false, null);
	        QueueingConsumer consumer=new QueueingConsumer(channel);
			channel.basicConsume(replyQueueName, false, consumer);  
			/*noAck = false，需要回复，接收到消息后，queue上的消息不会被清除，
			 * 直到调用channel.basicAck(deliveryTag, false); queue上的消息才会被清除 
			 * 而且，在当前连接断开以前，其它客户端将不能收到此queue上的消息*/
		    
		    while (true) {
	        	QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeOut);
	        	if (delivery == null) {
	        		response = null;
	        		break;
	        	}
	            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
	            	channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);   //delivery will be deleted from queue
	            	response = new String(delivery.getBody(),"utf-8");
	                break;
	            }
	        }
	    }catch(Exception e){
	    	mainlogger.error(e.toString(),e);
	    	if(e instanceof com.rabbitmq.client.AlreadyClosedException||!connection.isOpen()){
				 try{
					 connection.close();
				 }catch(Exception e2){}
				 try {
					connection=factory.newConnection();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			 }
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
	    }finally{
	    	if(channel!=null){
	    		try {
					channel.close();
				} catch (IOException e) {
				}
	    	}
	    	
	    }
	    return response;
	}
	
}
	


package org.epiclouds.message.impl;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.epiclouds.message.abstracts.AbstractMessageCall;
import org.epiclouds.spiders.config.manager.impl.CustomConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * the mq implementation
 * @author xianglong
 * @created 2015年6月4日 上午11:23:09
 * @version 1.0
 */
public class MQCallImpl extends AbstractMessageCall{
	private static Logger mainlogger = LoggerFactory.getLogger(MQCallImpl.class);
	
	private ConnectionFactory factory=null;
	
	@Resource
	private CustomConfigManager cmi;
	
	private Connection connection = null;
	
	public MQCallImpl() throws IOException{
		if(factory==null){
			factory = new ConnectionFactory();
		}
	}
	
	public void setConnection() throws IOException{
		factory.setHost(cmi.getValue("rabbit_host",String.class));
		factory.setUsername(cmi.getValue("rabbit_username",String.class));
		factory.setPassword(cmi.getValue("rabbit_password",String.class));
		factory.setAutomaticRecoveryEnabled(false);
		connection=factory.newConnection();
	}
	
	@Override
	public String getMessage(String... sources) {
		 Channel channel =null;
		 try{
			 if(connection==null){
				connection=factory.newConnection();
			 }
		    channel = connection.createChannel();
		    channel.queueDeclare(sources[0], false, false, false, null);
		    QueueingConsumer consumer = new QueueingConsumer(channel);
		    channel.basicConsume(sources[0], true, consumer);
		    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		     String message = new String(delivery.getBody(),"utf-8");
		     return message;
		 }catch(Exception e){
			 mainlogger.error(e.getLocalizedMessage(),e);
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
		return null;

	   
	}

	@Override
	public void sendMessage(String destination,String id, Object... others) {
		Channel channel = null;
	    try{
	    	if(connection==null){
				connection=factory.newConnection();
	    	}
		    channel = connection.createChannel();
		    channel.queueDeclare( destination, false, false, false, null);
		    BasicProperties  replyProps = new BasicProperties
             	     .Builder()
             	     .correlationId(id)
             	     .build();
		    channel.basicPublish("", destination, replyProps,
		    		JSONObject.toJSONString(others[0]).getBytes());
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
					e.printStackTrace();
				}
	    	}
	    }
	}

}

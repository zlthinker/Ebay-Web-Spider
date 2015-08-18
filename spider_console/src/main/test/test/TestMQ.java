package test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.epiclouds.message.impl.MQCallImpl;
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.command.abstracts.SpiderClassConfigBean;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;
import org.junit.Test;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class TestMQ {

	@Test
	public void testGetSpiderObjectConfigs(){
		List<SpiderObjectBean> ll=new LinkedList<SpiderObjectBean>();
		for(int i=0;i<5;i++){
			SpiderObjectBean cb=new SpiderObjectBean();
			cb.setName("ebay store"+i);
			cb.setIsrun(i%2==0);
			cb.setTotal_num(100);
			cb.setSpided_num(10+i);
			ll.add(cb);
		}
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.GETSPIDEROBJECTS);
		ccb.setOb(ll);
//		MQSender.send(ccb, "ok1"+"_result");
	}
	
/*	@Test
	public void testRemoveSpiderObjectSuccess(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.DELETESUCCESS);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
/*	@Test
	public void testRemoveSpiderObjectFail(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.DELETEFAILUE);
		ccb.setOb("object已经存在！");
		MQSender.send(ccb, "ok1"+"_result");
	}*/
	
	/*@Test
	public void testStartSpiderObjectSuccess(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STARTSUCCESS);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
	/*@Test
	public void testStartSpiderObjectFail(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STARTFAILURE);
		ccb.setOb("object已经存在！");
		MQSender.send(ccb, "ok1"+"_result");
<<<<<<< HEAD
	}*/
	
/*	@Test
	public void testStopSpiderObjectSuccess(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STOPSUCCESS);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
	/*@Test
	public void testStopSpiderObjectFail(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STOPFAILURE);
		ccb.setOb("object已经存在！");
		MQSender.send(ccb, "ok1"+"_result");
=======
>>>>>>> d66f49640da2e17fba9d056d5b9959d0362dde2d
	}*/


	
/*	@Test
<<<<<<< HEAD
=======
	public void testStopSpiderObjectSuccess(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STOPSUCCESS);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
	/*@Test
	public void testStopSpiderObjectFail(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.STOPFAILURE);
		ccb.setOb("object已经存在！");
		MQSender.send(ccb, "ok1"+"_result");
	}*/


	
/*	@Test
>>>>>>> d66f49640da2e17fba9d056d5b9959d0362dde2d
	public void testAddSpiderObjectConfigs(){
		List<AddSpiderObjectBean> ll=new LinkedList<AddSpiderObjectBean>();
		for(int i=0;i<5;i++){
			AddSpiderObjectBean cb=new AddSpiderObjectBean();
			cb.setDesc("浣犲ソ"+i);
			cb.setName("hello"+i);
			ll.add(cb);
		}
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.GETADDCONFIGSUCCESS);
		ccb.setOb(ll);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
/*	@Test
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> d66f49640da2e17fba9d056d5b9959d0362dde2d
	public void testAddSpiderObjectSuccess(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.ADDSUCCESS);
		MQSender.send(ccb, "ok1"+"_result");
	}*/
/*	@Test
	public void testAddSpiderObjectFail(){
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.ADDFAILURE);
		ccb.setOb("object已经存在！");
		MQSender.send(ccb, "ok1"+"_result");
	}*/
/*	@Test
<<<<<<< HEAD
>>>>>>> d66f49640da2e17fba9d056d5b9959d0362dde2d
=======
>>>>>>> d66f49640da2e17fba9d056d5b9959d0362dde2d
	public void testViewSpiderClassConfig(){
		List<SpiderClassConfigBean> ll=new LinkedList<SpiderClassConfigBean>();
		for(int i=0;i<5;i++){
			SpiderClassConfigBean cb=new SpiderClassConfigBean();
			cb.setName("ebay store"+i);
			cb.setIsrun(i%2==0);
			cb.setDesc("浣犲洖绛�"+i);
			cb.setValue("澶хキ鍙哥殑");
			ll.add(cb);
		}
		ConsoleCommandBean ccb=new ConsoleCommandBean();
		ccb.setCommand(ConsoleCommand.GETCONFIGSUCCESS);
		ccb.setOb(ll);
		MQSender.send(ccb, "ok1"+"_result");
<<<<<<< HEAD
<<<<<<< HEAD
		System.out.println("After send");
	} */


	

	public static void main (String args[]) throws Exception {
		ConnectionFactory connFac = new ConnectionFactory();
		connFac.setHost("106.3.38.50");
		connFac.setUsername("yuanshuju");
		connFac.setPassword("123Yuanshuju456");
		Connection connection = connFac.newConnection();
		Channel channel = connection.createChannel();
		String response;
		String corrId = UUID.randomUUID().toString();
		long timeout = 30 * 1000;
	
		String queueName = "console_queue";
//	    String replyQueueName = channel.queueDeclare().getQueue();
//	    System.out.println("replyQueueName = " + replyQueueName);
	    QueueingConsumer consumer = new QueueingConsumer(channel); 
//	    channel.basicConsume(replyQueueName, true, consumer);
	    
	    BasicProperties props = new BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo("console_queue_result")
                .build(); 
  
	    SpiderClassBean sb = new SpiderClassBean();
	    sb.setDesc("test");
	    sb.setName("init_test");
	    sb.setQueue_name("ebayTest");
	    ConsoleCommandBean cb = new ConsoleCommandBean(ConsoleCommand.INIT, sb);
	    String message = JSONObject.toJSONString(cb);
	    if (channel == null)
	    	System.out.println("channel is null.");
	    channel.queueDeclare(queueName, false, false, false, null);
        channel.basicPublish("", queueName, null, message.getBytes());
        System.out.println("Send message: " + message);
	 
 /*       while (true) {
        	QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        	if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response = new String(delivery.getBody());
                System.out.println("Response is " + response);
                break;
            }*/
     /*   	if (delivery == null) {
        		System.err.println("Timeout: No response.");
        		response = null;
        		break;
        	}*/
            
       
		
		
	/*	ConnectionFactory connFac = new ConnectionFactory();
		connFac.setHost("127.0.0.1");
		connFac.setUsername("guest");
		connFac.setPassword("guest");
		Connection conn = connFac.newConnection();
		Channel chan = conn.createChannel();
		String queuename = "ebay";
		chan.queueDeclare(queuename, true, false, false, null);
		
		ConsoleCommandBean cmdBean = new ConsoleCommandBean(ConsoleCommand.ADD, new Object());
		String msg = JSONObject.toJSONString(cmdBean);  
        
        //发送消息  
        chan.basicPublish("", queuename , null , msg.getBytes());  
          
        System.out.println("send message[" + msg + "] to "+ queuename +" success!");  
          
        chan.close();   
        conn.close();*/
		
	}




}

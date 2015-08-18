package org.epiclouds.test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Resource;

import org.epiclouds.spiders.config.manager.abstracts.ConfigManagerInterface;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

@Component
public class TestMain {
	@Resource
	private Test2Interface t2;
	@Resource
	private static Connection connection;
	
	private long t1;
	public void print(){
		System.err.println("TestMain");
		t2.print();
	}
	
	public static void main(String[] args) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, IOException{
		//ApplicationContext context=new ClassPathXmlApplicationContext("spring.xml");

		/*TestMain tm=context.getBean(TestMain.class);
		tm.print();
		ConfigManagerInterface cmi=context.getBean(ConfigManagerInterface.class);
		System.err.println(cmi.getValue("spider_num"));*/
		
		/*List<String> ss=JSONObject.parseArray("[123,12222]", String.class);
		System.err.println(ss.get(0));
		Class<TestMain> tm=TestMain.class;
		TestMain tt=tm.newInstance();
		Field fd=tm.getDeclaredField("t1");
		fd.set(tt, "2");
		System.err.println(tt.t1);*/
		ConnectionFactory factory=null;
		factory=new ConnectionFactory();
		factory.setHost("106.3.38.50");
		factory.setUsername("yuanshuju");
		factory.setPassword("123Yuanshuju456");
		factory.setAutomaticRecoveryEnabled(true);
		connection=factory.newConnection();
		
		sendMessage("console_queue6", "1", "56773");
		System.exit(0);
	}
	
	
	public static void sendMessage(String destination,String id, Object... others) {
		Channel channel = null;
	    try{
		    channel = connection.createChannel();
		    channel.queueDeclare( destination, false, false, false, null);
		    BasicProperties  replyProps = new BasicProperties
             	     .Builder()
             	     .correlationId(id)
             	     .build();
		    channel.basicPublish("", destination, replyProps,
		    		JSONObject.toJSONString(others[0]).getBytes());
	    }catch(Exception e){
	    	e.printStackTrace();
	    }finally{
	    	if(channel!=null){
	    		try {
					channel.close();
				} catch (IOException e) {
				}
	    	}
	    }
	}
}

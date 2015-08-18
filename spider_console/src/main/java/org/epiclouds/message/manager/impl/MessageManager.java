package org.epiclouds.message.manager.impl;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.epiclouds.message.abstracts.AbstractMessageCall;
import org.epiclouds.message.abstracts.MessageCallInterface;
import org.epiclouds.message.impl.MQCallImpl;
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.util.ConsoleConfig;
//import org.epiclouds.spiders.config.manager.abstracts.ConfigManagerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * The manager of message
 * @author xianglong
 * @created 2015楠烇拷6閺堬拷4閺冿拷 娑撳﹤宕�11:31:54
 * @version 1.0
 */
@Component
public class MessageManager implements Runnable{

	private static Logger mainlogger = LoggerFactory.getLogger(MQCallImpl.class);
	@Resource
	private AbstractMessageCall messageCall;
	@Resource
	private ConsoleCommandManager manager ;
	
	
	public MessageManager(){
		
	}
	
	@PostConstruct
	public void init(){
		Executors.newSingleThreadExecutor().execute(this);
	}
	
	
	@Override
	public void run() {
	    while (true) {
	    	 try{
	    		  Thread.sleep(1000*3);
			      String message = messageCall.getMessage(ConsoleConfig.getReceive_queue_name());
			      ConsoleCommandBean commandBean = JSONObject.parseObject(message, ConsoleCommandBean.class);
			      System.err.println(ConsoleConfig.getReceive_queue_name());
			      if(commandBean.getCommand()==ConsoleCommand.INIT){
			    	  Object obj = commandBean.getOb();
			    	  SpiderClassBean classBean = JSONObject.parseObject(JSONObject.toJSONString(obj), SpiderClassBean.class);
			    	  SpiderStatusManager.putBackSpider(classBean);
			      }
			 
	    	 }catch(Exception e){
	 	    	mainlogger.error(e.toString());
	 	    }		      
	    }
	}
	
}

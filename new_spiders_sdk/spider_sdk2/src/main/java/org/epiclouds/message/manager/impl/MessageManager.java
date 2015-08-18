package org.epiclouds.message.manager.impl;

import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.epiclouds.message.abstracts.MessageCallInterface;
import org.epiclouds.message.impl.*;
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.config.manager.abstracts.ConfigManagerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

/**
 * The manager of message
 * @author xianglong
 * @created 2015年6月4日 上午11:31:54
 * @version 1.0
 */
@Component
public class MessageManager implements Runnable{

	private static Logger mainlogger = LoggerFactory.getLogger(MessageManager.class);
	@Resource
	private MessageCallInterface messagecall;
	
	@Resource
	private ConsoleCommandManager manager;
	
	@Resource
	private ConfigManagerInterface cmi;
	
	
	public MessageManager(){

	}

	
	@PostConstruct
	public void init(){
		Executors.newSingleThreadExecutor().execute(this);
	}
	@Override
	public void run() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.err.println("init message will sended!");

		Object init=manager.handleCommand(new ConsoleCommandBean(ConsoleCommand.INIT, null));
		messagecall.sendMessage(cmi.getValue("console_queue_name",String.class),"1",init); 
		System.err.println("init message sended!"+init);
	    while (true) {
	    	 try{
	    		 String message=messagecall.getMessage(cmi.getValue("name",String.class));
			     ConsoleCommandBean commandBean=JSONObject.parseObject(message, ConsoleCommandBean.class);
			     Object re=manager.handleCommand(commandBean);
			     String suffix="_result";
			     messagecall.sendMessage(cmi.getValue("name",String.class)+suffix,commandBean.getId(),re);
	    	 }catch(Exception e){
	 	    	mainlogger.error(e.toString());
	 	    }
		      
	    }
	}
	
}

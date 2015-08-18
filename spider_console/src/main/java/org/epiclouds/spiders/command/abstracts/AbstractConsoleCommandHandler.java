package org.epiclouds.spiders.command.abstracts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.annotation.Resource;

import org.epiclouds.message.abstracts.AbstractMessageCall;
import org.epiclouds.message.impl.MQCallImpl;
import org.epiclouds.spiders.main.RunTera;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.QueueingConsumer;

/**
 * the command handler class
 * @author xianglong
 *
 */
public abstract class AbstractConsoleCommandHandler implements ConsoleCommandHandler{
	private static Logger mainlogger = LoggerFactory.getLogger(RunTera.class);
	/**
	 * the console command
	 */
	private final ConsoleCommand command;
	//MessageCallInterface
	@Resource
	private AbstractMessageCall messageCall;
//	private ConnectionFactory factory=null;
	
	public AbstractConsoleCommandHandler(ConsoleCommand command){
		this.command=command;
	}
	


	public AbstractMessageCall getMessageCall() {
		return messageCall;
	}



	public void setMessageCall(AbstractMessageCall messageCall) {
		this.messageCall = messageCall;
	}



	public ConsoleCommand getCommand() {
		return command;
	}
	public    enum ConsoleCommand {
		START,STARTSUCCESS,STARTFAILURE,STARTALL,STARTALLSUCCESS,STARTALLFAILURE,
		STOP,STOPSUCCESS,STOPFAILURE,
		ADD,ADDSUCCESS,ADDFAILURE,
		DELETE,DELETESUCCESS,DELETEFAILURE,
		UPDATE,UPDATESUCCESS,UPDATEFAILURE,
		GETSINGLESPIDER,GETSINGLESPIDERSUCCESS,GETSINGLESPIDERFAILURE,
		GETSPIDEROBJECTS,GETSPIDEROBJECTSSUCCESS,GETSPIDEROBJECTSFAILURE,
		MODIFYCONFIG,MODIFYCONFIGSUCCESS,MODIFYCONFIGFAILURE,
		GETCONFIG,GETCONFIGSUCCESS,GETCONFIGFAILURE,
		INIT, GETSTATE
	}
	
	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,Object... args) {
		Object obj =bean.getOb();
	    SpiderClassBean classBean = JSONObject.parseObject(JSONObject.toJSONString(obj), SpiderClassBean.class);
		if(classBean==null) return null;
		ConsoleCommandBean bean2=new ConsoleCommandBean();
		bean2.setCommand(bean.getCommand());
		bean2.setOb(args);
		

 
	    String requestQueueName = classBean.getQueue_name();  


	    String response = messageCall.sendAndReceive(requestQueueName, bean2);
        if (response == null) {
        	System.err.println("No response is received at console.");
        	return null;
        }
        ConsoleCommandBean commandBean = JSONObject.parseObject(response, ConsoleCommandBean.class);
        
        return commandBean;
		

	}
}

package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;

import com.alibaba.fastjson.JSONObject;
/**
 * The stop command handler
 * @author xianglong
 *
 */
public class StopCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public StopCommandHandlerImpl() {
		super(ConsoleCommand.STOP);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,ConsoleCommandManager manager,
			Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<String> ll=JSONObject.parseArray(JSONObject.toJSONString(bean.getOb()), 
					String.class);
			manager.getSpiderManager().stop(ll.get(0));
			re.setCommand(ConsoleCommand.STOPSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.STOPFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

	
}

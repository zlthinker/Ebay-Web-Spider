package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;

import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class DeleteSpiderObjectHandlerImpl extends  AbstractConsoleCommandHandler{

	public DeleteSpiderObjectHandlerImpl() {
		super(ConsoleCommand.DELETE);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,ConsoleCommandManager manager,
			Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<String> ll=JSONObject.parseArray(JSONObject.toJSONString(bean.getOb()), 
					String.class);
			manager.getSpiderManager().delete(ll.get(0));
			re.setCommand(ConsoleCommand.DELETESUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.DELETEFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

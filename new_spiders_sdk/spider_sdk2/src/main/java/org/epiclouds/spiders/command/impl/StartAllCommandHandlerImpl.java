package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author zyc
 *
 */

public class StartAllCommandHandlerImpl extends AbstractConsoleCommandHandler{

	public StartAllCommandHandlerImpl() {
		super(ConsoleCommand.STARTALL);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		// TODO Auto-generated method stub
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			manager.getSpiderManager().startAll();
			re.setCommand(ConsoleCommand.STARTALLSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.STARTALLFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

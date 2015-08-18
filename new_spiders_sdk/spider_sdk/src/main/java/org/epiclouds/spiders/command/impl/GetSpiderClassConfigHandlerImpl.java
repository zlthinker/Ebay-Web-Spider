package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.config.field.abstracts.AbstractField;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class GetSpiderClassConfigHandlerImpl extends  AbstractConsoleCommandHandler{

	public GetSpiderClassConfigHandlerImpl() {
		super(ConsoleCommand.GETCONFIG);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<AbstractField> list=manager.getCmi().getAllFields();
			re.setOb(list);
			re.setCommand(ConsoleCommand.GETCONFIGSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.GETCONFIGFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}


}

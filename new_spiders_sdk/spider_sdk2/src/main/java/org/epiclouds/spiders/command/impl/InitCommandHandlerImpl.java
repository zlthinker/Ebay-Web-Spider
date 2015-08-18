package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
/**
 * The stop command handler
 * @author xianglong
 *
 */
public class InitCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public InitCommandHandlerImpl() {
		super(ConsoleCommand.INIT);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,ConsoleCommandManager manager,Object... args) {
		try {
			ConsoleCommandBean re=new ConsoleCommandBean();
			SpiderClassBean scb=new SpiderClassBean();
			scb.setName(manager.getCmi().getValue("name",String.class));
			scb.setDesc( manager.getCmi().getValue("desc",String.class));
			scb.setQueue_name(manager.getCmi().getValue("name",String.class));
			re.setOb(scb);
			scb.setAddConfigs(manager.getSpiderManager().getSpiderObjectConfig(null));
			re.setCommand(ConsoleCommand.INIT);
			return re;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

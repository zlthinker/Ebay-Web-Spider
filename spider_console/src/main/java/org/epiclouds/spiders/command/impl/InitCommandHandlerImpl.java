package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.spiderbean.util.SpiderStatusManager;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.springframework.stereotype.Component;
/**
 * The stop command handler
 * @author xianglong
 *
 */
@Component
public class InitCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public InitCommandHandlerImpl() {
		super(ConsoleCommand.INIT);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,Object... args) {
		SpiderClassBean o=(SpiderClassBean)bean.getOb();
		if(o==null) return null;
		SpiderStatusManager.putBackSpider(o);
		return null;
	}
}

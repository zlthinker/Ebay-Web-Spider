package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.spiderbean.util.SpiderClassBean;
import org.epiclouds.spiders.util.ConsoleConfig;
import org.springframework.stereotype.Component;
/**
 * The start command handler
 * @author xianglong
 *
 */
@Component
public class DeleteSpiderObjectHandlerImpl extends  AbstractConsoleCommandHandler{

	public DeleteSpiderObjectHandlerImpl() {
		super(ConsoleCommand.DELETE);
	}

}

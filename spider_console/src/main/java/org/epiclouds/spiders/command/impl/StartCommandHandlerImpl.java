package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.springframework.stereotype.Component;
/**
 * The start command handler
 * @author xianglong
 *
 */
@Component
public class StartCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public StartCommandHandlerImpl() {
		super(ConsoleCommand.START);
	}

	
}

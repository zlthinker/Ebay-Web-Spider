package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.springframework.stereotype.Component;
/**
 * The start command handler
 * @author zyc
 *
 */
@Component
public class StartAllCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public StartAllCommandHandlerImpl() {
		super(ConsoleCommand.STARTALL);
	}

	
}

package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.springframework.stereotype.Component;
/**
 * The stop command handler
 * @author xianglong
 *
 */
@Component
public class StopCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public StopCommandHandlerImpl() {
		super(ConsoleCommand.STOP);
	}

	
}

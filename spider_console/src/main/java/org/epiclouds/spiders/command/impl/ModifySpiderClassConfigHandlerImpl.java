package org.epiclouds.spiders.command.impl;
 
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.springframework.stereotype.Component;
/**
 * The start command handler
 * @author xianglong
 *
 */
@Component
public class ModifySpiderClassConfigHandlerImpl extends  AbstractConsoleCommandHandler{

	public ModifySpiderClassConfigHandlerImpl() {
		super(ConsoleCommand.MODIFYCONFIG);
	}
	

}

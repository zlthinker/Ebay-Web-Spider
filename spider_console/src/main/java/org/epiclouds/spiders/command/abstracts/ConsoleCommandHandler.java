package org.epiclouds.spiders.command.abstracts;

/**
 * handle the command interface
 * @author xianglong
 * 
 */
public interface ConsoleCommandHandler {
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,Object... args);
}

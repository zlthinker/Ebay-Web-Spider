package org.epiclouds.message.abstracts;

import java.io.IOException;

import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;

public interface MessageCallInterface {
	/**
	 * get the message from theb source
	 * @param sources
	 * @return the message
	 * @throws IOException 
	 */
	public String getMessage(String... sources) throws IOException;
	/**
	 * send message to a destination
	 * @param destination
	 * @param others
	 * @throws IOException 
	 */
	public void sendMessage(String destination,Object... others) throws IOException;
	
	public String sendAndReceive (String destination, Object... others);
	
}

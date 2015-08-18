package org.epiclouds.spiders.command.abstracts;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;

/**
 * console command bean
 * send or receieve from the message queue
 * @author xianglong
 *
 */
public class ConsoleCommandBean {
	private String id;
	/**
	 * the console's command
	 */
	private ConsoleCommand command;
	/**
	 * the request will send or response we receive
	 */
	private Object ob;
	
	public ConsoleCommandBean(ConsoleCommand command,Object o){
		this.command=command;
		this.setOb(o);
	}
	public ConsoleCommandBean(){}
	
	public ConsoleCommand getCommand() {
		return command;
	}
	public void setCommand(ConsoleCommand command) {
		this.command = command;
	}
	public Object getOb() {
		return ob;
	}
	public void setOb(Object ob) {
		this.ob = ob;
	}

	public String toString(){
		return this.command+":"+ob;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}

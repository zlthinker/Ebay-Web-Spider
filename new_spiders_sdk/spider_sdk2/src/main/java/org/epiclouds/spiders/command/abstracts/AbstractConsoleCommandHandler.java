package org.epiclouds.spiders.command.abstracts;


/**
 * the command handler class
 * @author xianglong
 *
 */
public abstract class AbstractConsoleCommandHandler implements ConsoleCommandHandler{
	
	/**
	 * the console command
	 */
	private final ConsoleCommand command;
	
	public AbstractConsoleCommandHandler(ConsoleCommand command){
		this.command=command;
	}
	
	
	public ConsoleCommand getCommand() {
		return command;
	}
	public  static   enum ConsoleCommand {
		START,STARTSUCCESS,STARTFAILURE,STARTALL,STARTALLSUCCESS,STARTALLFAILURE,
		STOP,STOPSUCCESS,STOPFAILURE,
		ADD,ADDSUCCESS,ADDFAILURE,
		DELETE,DELETESUCCESS,DELETEFAILURE,
		UPDATE,UPDATESUCCESS,UPDATEFAILURE,
		GETSINGLESPIDER,GETSINGLESPIDERSUCCESS,GETSINGLESPIDERFAILURE,
		GETSPIDEROBJECTS,GETSPIDEROBJECTSSUCCESS,GETSPIDEROBJECTSFAILURE,
		MODIFYCONFIG,MODIFYCONFIGSUCCESS,MODIFYCONFIGFAILURE,
		GETCONFIG,GETCONFIGSUCCESS,GETCONFIGFAILURE,
		INIT
	}

}

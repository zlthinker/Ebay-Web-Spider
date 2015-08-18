package org.epiclouds.spiders.command.abstracts;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.main.RunTera;
import org.epiclouds.spiders.util.ClassPathScanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * the console command manager.
 * @author xianglong
 *
 */

public class ConsoleCommandManager {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory
            .getLogger(ConsoleCommandManager.class);
	/**
	 * the handler map,init in constructor
	 */
	private final EnumMap<ConsoleCommand, AbstractConsoleCommandHandler> handler_map=
			new EnumMap<AbstractConsoleCommandHandler.ConsoleCommand, AbstractConsoleCommandHandler>(ConsoleCommand.class);
	
	private static ConsoleCommandManager manager;
	
	private AtomicBoolean ii=new AtomicBoolean(false);
	
	private void init(){
		manager=this;
	}
	
	private void initHandler(){
		Map<String, AbstractConsoleCommandHandler>  map=RunTera.context.getBeansOfType(AbstractConsoleCommandHandler.class);
		for(AbstractConsoleCommandHandler handler:map.values()){
			handler_map.put(handler.getCommand(), handler);
		}
	}
	
	/**
	 * handle the command, based on the command, from the handler_map get the handler and execute the method
	 */
	public ConsoleCommandBean handleCommand(ConsoleCommandBean commandBean,Object... args){
		if(ii.compareAndSet(false, true)){
			initHandler();
		}
		ConsoleCommandHandler h=handler_map.get(commandBean.getCommand());
		if(h!=null){
			return h.handleCommand(commandBean,args);
		}
		return null;
	}
	
	public String toString(){
		if(ii.compareAndSet(false, true)){
			initHandler();
		}
		Set<ConsoleCommand> set=handler_map.keySet();
		Object[] cs=set.toArray();
		String str="";
		for(Object o:cs){
			str+=o+":";
		}
		return str;
	}

	public static ConsoleCommandManager getManager() {
		return manager;
	}
}

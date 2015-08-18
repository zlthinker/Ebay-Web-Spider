package org.epiclouds.spiders.command.abstracts;

import java.util.EnumMap;
import java.util.Set;

import javax.annotation.Resource;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.config.manager.abstracts.ConfigManagerInterface;
import org.epiclouds.spiders.spiderobject.manager.abstracts.SpiderObjectManagerInterface;
import org.epiclouds.spiders.util.ClassPathScanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * the console command manager.
 * @author xianglong
 *
 */
@Component
public class ConsoleCommandManager {
	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory
            .getLogger(ConsoleCommandManager.class);
	/**
	 * the handler map,init in constructor
	 */
	private final EnumMap<ConsoleCommand, ConsoleCommandHandler> handler_map=
			new EnumMap<AbstractConsoleCommandHandler.ConsoleCommand, ConsoleCommandHandler>(ConsoleCommand.class);
	
	@Resource
	private ConfigManagerInterface cmi;
	
	@Resource 
	private SpiderObjectManagerInterface spiderManager;
	
	/**
	 * the class scanner
	 */
	private static final ClassPathScanHandler scanner =new ClassPathScanHandler();

	
	public ConsoleCommandManager(){

		Set<Class<?>> set=scanner.getPackageAllClasses("org.epiclouds.spiders.command.impl", false);
		for(Class<?> c:set){
			if(c.getSuperclass().getName().equals(AbstractConsoleCommandHandler.class.getName())){
				try {
					AbstractConsoleCommandHandler ac=(AbstractConsoleCommandHandler)c.newInstance();
					handler_map.put(ac.getCommand(), ac);
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	/**
	 * handle the command, based on the command, from the handler_map get the handler and execute the method
	 */
	public ConsoleCommandBean handleCommand(ConsoleCommandBean commandBean,Object... args){
		ConsoleCommandHandler h=handler_map.get(commandBean.getCommand());
		if(h!=null){
			return h.handleCommand(commandBean,this,args);
		}
		return null;
	}
	public ConfigManagerInterface getCmi() {
		return cmi;
	}

	public void setCmi(ConfigManagerInterface cmi) {
		this.cmi = cmi;
	}
	public SpiderObjectManagerInterface getSpiderManager() {
		return spiderManager;
	}

	public void setSpiderManager(SpiderObjectManagerInterface spiderManager) {
		this.spiderManager = spiderManager;
	}
	
	public String toString(){
		Set<ConsoleCommand> set=handler_map.keySet();
		Object[] cs=set.toArray();
		String str="";
		for(Object o:cs){
			str+=o+":";
		}
		return str;
	}
}

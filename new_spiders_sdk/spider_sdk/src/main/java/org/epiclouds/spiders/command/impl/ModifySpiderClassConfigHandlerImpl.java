package org.epiclouds.spiders.command.impl;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class ModifySpiderClassConfigHandlerImpl extends  AbstractConsoleCommandHandler{

	public ModifySpiderClassConfigHandlerImpl() {
		super(ConsoleCommand.MODIFYCONFIG);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			JSONObject o=((JSONArray)bean.getOb()).getJSONObject(0);
			String name=o.getString("name");
			String value=o.getString("value");
			manager.getCmi().set(name, value);
			re.setCommand(ConsoleCommand.MODIFYCONFIGSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.MODIFYCONFIGFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

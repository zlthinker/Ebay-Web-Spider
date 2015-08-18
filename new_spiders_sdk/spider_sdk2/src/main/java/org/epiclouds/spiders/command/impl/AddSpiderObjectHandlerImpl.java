package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class AddSpiderObjectHandlerImpl extends  AbstractConsoleCommandHandler{

	public AddSpiderObjectHandlerImpl() {
		super(ConsoleCommand.ADD);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<AddSpiderObjectBean> values=JSONObject.parseArray(JSONObject.toJSONString(((JSONArray)bean.getOb()).get(0)), 
					AddSpiderObjectBean.class);
			AbstractSpiderObject asb=manager.getSpiderManager().createSpiderObject(values);
			manager.getSpiderManager().add(asb);
			re.setCommand(ConsoleCommand.ADDSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.ADDFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}


	

}

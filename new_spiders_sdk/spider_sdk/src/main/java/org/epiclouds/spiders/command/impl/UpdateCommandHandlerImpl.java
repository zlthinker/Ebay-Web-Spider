package org.epiclouds.spiders.command.impl;

import java.util.List;
import java.util.UUID;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler.ConsoleCommand;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class UpdateCommandHandlerImpl extends  AbstractConsoleCommandHandler{

	public UpdateCommandHandlerImpl() {
		super(ConsoleCommand.UPDATE);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		// TODO Auto-generated method stub
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<AddSpiderObjectBean> values=JSONObject.parseArray(JSONObject.toJSONString(((JSONArray)bean.getOb()).get(0)), 
					AddSpiderObjectBean.class);
			AbstractSpiderObject asb=manager.getSpiderManager().createSpiderObject(values);
			String id=asb.getId();
			asb.setId(UUID.randomUUID().toString());
			manager.getSpiderManager().update(id,asb);
			re.setCommand(ConsoleCommand.UPDATESUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.UPDATEFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

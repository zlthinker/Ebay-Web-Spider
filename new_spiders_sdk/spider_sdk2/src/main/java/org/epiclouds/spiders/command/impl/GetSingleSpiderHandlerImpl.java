package org.epiclouds.spiders.command.impl;

import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;

import com.alibaba.fastjson.JSONObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class GetSingleSpiderHandlerImpl extends  AbstractConsoleCommandHandler{

	public GetSingleSpiderHandlerImpl() {
		super(ConsoleCommand.GETSINGLESPIDER);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<String> ll=JSONObject.parseArray(JSONObject.toJSONString(bean.getOb()), 
					String.class);
			if(ll.get(0)==null){
				throw new Exception("The name is null");
			}
			List<AddSpiderObjectBean> list=manager.getSpiderManager().getSpiderObjectConfig(ll.get(0));
			re.setOb(list);
			re.setCommand(ConsoleCommand.GETSINGLESPIDERSUCCESS);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.GETSINGLESPIDERFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

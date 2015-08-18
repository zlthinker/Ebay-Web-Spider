package org.epiclouds.spiders.command.impl;

import java.util.LinkedList;
import java.util.List;

import org.epiclouds.spiders.command.abstracts.AbstractConsoleCommandHandler;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandBean;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.spiderbean.util.SpiderObjectBean;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
/**
 * The start command handler
 * @author xianglong
 *
 */
public class GetSpiderObjectsHandlerImpl extends  AbstractConsoleCommandHandler{

	public GetSpiderObjectsHandlerImpl() {
		super(ConsoleCommand.GETSPIDEROBJECTS);
	}

	@Override
	public ConsoleCommandBean handleCommand(ConsoleCommandBean bean,
			ConsoleCommandManager manager, Object... args) {
		// TODO Auto-generated method stub
		ConsoleCommandBean re=new ConsoleCommandBean();
		try{
			List<AbstractSpiderObject> tmp=manager.getSpiderManager().getAllSpiderObjects();
			List<SpiderObjectBean> sob=new LinkedList<SpiderObjectBean>();
			List<SpiderObjectBean> run=new LinkedList<SpiderObjectBean>();
			List<SpiderObjectBean> notrun=new LinkedList<SpiderObjectBean>();
			for(AbstractSpiderObject ab:tmp){
				if(ab.isrun()){
					run.add(ab.toSpiderObjectBean());
				}else{
					notrun.add(ab.toSpiderObjectBean());
				}
			}
			sob.addAll(run);
			sob.addAll(notrun);
			re.setCommand(ConsoleCommand.GETSPIDEROBJECTSSUCCESS);
			re.setOb(sob);
		}catch(Exception e){
			re.setCommand(ConsoleCommand.GETSPIDEROBJECTSFAILURE);
			re.setOb(e.getLocalizedMessage());
		}
		return re;
	}

}

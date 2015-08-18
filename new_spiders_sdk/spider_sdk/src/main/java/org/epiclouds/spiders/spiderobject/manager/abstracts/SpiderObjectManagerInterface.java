package org.epiclouds.spiders.spiderobject.manager.abstracts;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;
import org.epiclouds.spiders.spiderobject.abstracts.*;

/**
 * the spider class manager interface
 * @author xianglong
 *
 */
public interface SpiderObjectManagerInterface {
	

	public List<AddSpiderObjectBean> getSpiderObjectConfig(String name)throws Exception;
	public List<AbstractSpiderObject> getAllSpiderObjects();
	public void add(AbstractSpiderObject spiderObejct) throws Exception;
	public void delete(String name) throws Exception;
	public void start(String name) throws Exception;
	public void startAll() throws Exception;
	public void stop(String name) throws Exception;
	public void initFromStorage() throws Exception;
	public void update(String name,AbstractSpiderObject spiderObject) throws Exception;
	public AbstractSpiderObject createSpiderObject(List<AddSpiderObjectBean> values) throws Exception;
	public void removeRunningMapOrWaitingList(AbstractSpiderObject abstractSpiderObject);
	public void runOnce() throws Exception;
	public void setClassz(Class<? extends AbstractSpiderObject> classz);
}

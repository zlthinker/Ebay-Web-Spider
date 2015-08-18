package org.epiclouds.spiders.spiderobject.manager.abstracts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

/**
 * Spider Object manager  abstract class
 * @author xianglong
 * @created 2015年6月19日 上午11:40:26
 * @version 1.0
 */
public abstract class AbstractSpiderObjectManager implements SpiderObjectManagerInterface{
	
	protected Map<Class,SpiderManagerBean> managerBeans=new ConcurrentHashMap<Class,SpiderManagerBean>();
	protected volatile ConcurrentHashMap<String,AbstractSpiderObject> totalMap=new ConcurrentHashMap<String, AbstractSpiderObject>();
	protected volatile Class<? extends AbstractSpiderObject> classz;
	@Resource
	protected DBMangerInterface dbManager;
	@Resource
	protected AbstractConfigManager configManager;

	public Map<Class, SpiderManagerBean> getManagerBeans() {
		return managerBeans;
	}
	public void setManagerBeans(Map<Class, SpiderManagerBean> managerBeans) {
		this.managerBeans = managerBeans;
	}
	public ConcurrentHashMap<String, AbstractSpiderObject> getTotalMap() {
		return totalMap;
	}
	public void setTotalMap(ConcurrentHashMap<String, AbstractSpiderObject> totalMap) {
		this.totalMap = totalMap;
	}
	public DBMangerInterface getDbManager() {
		return dbManager;
	}
	public void setDbManager(DBMangerInterface dbManager) {
		this.dbManager = dbManager;
	}
	public AbstractConfigManager getConfigManager() {
		return configManager;
	}
	public void setConfigManager(AbstractConfigManager configManager) {
		this.configManager = configManager;
	}
	
	
}

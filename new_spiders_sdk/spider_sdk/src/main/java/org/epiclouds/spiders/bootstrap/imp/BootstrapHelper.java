package org.epiclouds.spiders.bootstrap.imp;

import javax.annotation.Resource;

import org.epiclouds.handlers.CrawlerClient;
import org.epiclouds.handlers.util.ProxyStateBean;
import org.epiclouds.message.manager.impl.MessageManager;
import org.epiclouds.spiders.command.abstracts.ConsoleCommandManager;
import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.spiderobject.manager.abstracts.AbstractSpiderObjectManager;
import org.springframework.stereotype.Component;

@Component
public class BootstrapHelper {
	
	@Resource
	private  CrawlerClient cc;
	@Resource
	private MessageManager mm;
	@Resource
	private ConsoleCommandManager ccm;
	@Resource
	private AbstractConfigManager acfm;
	@Resource
	private DBMangerInterface dbm;
	@Resource
	private AbstractSpiderObjectManager spiderManager;
	
	private ProxyStateBean pxy;
	
	public BootstrapHelper(){
		
	}


	public CrawlerClient getCc() {
		return cc;
	}


	public void setCc(CrawlerClient cc) {
		this.cc = cc;
	}


	public MessageManager getMm() {
		return mm;
	}

	public void setMm(MessageManager mm) {
		this.mm = mm;
	}

	public ConsoleCommandManager getCcm() {
		return ccm;
	}

	public void setCcm(ConsoleCommandManager ccm) {
		this.ccm = ccm;
	}

	public AbstractConfigManager getAcfm() {
		return acfm;
	}

	public void setAcfm(AbstractConfigManager acfm) {
		this.acfm = acfm;
	}

	public DBMangerInterface getDbm() {
		return dbm;
	}

	public void setDbm(DBMangerInterface dbm) {
		this.dbm = dbm;
	}

	public AbstractSpiderObjectManager getSpiderManager() {
		return spiderManager;
	}

	public void setSpiderManager(AbstractSpiderObjectManager spiderManager) {
		this.spiderManager = spiderManager;
	}


	public ProxyStateBean getPxy() {
		return pxy;
	}


	public void setPxy(ProxyStateBean pxy) {
		this.pxy = pxy;
	}

}

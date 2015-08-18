package org.epiclouds.spiders.spiderbean.util;

import java.util.LinkedList;
import java.util.List;

import org.epiclouds.spiders.command.abstracts.AddSpiderObjectBean;

/**
 * This Bean Contains spider class info
 * @author xianglong
 *
 */
public class SpiderClassBean {
	/**
	 * the name of spider class
	 */
	private String name;
	/**
	 * the description info of the spider class
	 */
	private String desc;
	/**
	 * the queue name which spider class depend on
	 */
	private String queue_name;
	
	/**
	 * add configs
	 */
	private List<AddSpiderObjectBean> addConfigs=new LinkedList<AddSpiderObjectBean>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getQueue_name() {
		return queue_name;
	}
	public void setQueue_name(String queue_name) {
		this.queue_name = queue_name;
	}
	public List<AddSpiderObjectBean> getAddConfigs() {
		return addConfigs;
	}
	public void setAddConfigs(List<AddSpiderObjectBean> addConfigs) {
		this.addConfigs = addConfigs;
	}
	
}

package org.epiclouds.spiders.command.abstracts;

/**
 * 
 * @author xianglong
 *
 */
public class SpiderClassConfigBean {
	String name;
	String desc;
	String value;
	boolean isrun;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isIsrun() {
		return isrun;
	}
	public void setIsrun(boolean isrun) {
		this.isrun = isrun;
	}
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
	
}

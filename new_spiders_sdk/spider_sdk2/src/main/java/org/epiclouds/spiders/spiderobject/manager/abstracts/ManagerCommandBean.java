package org.epiclouds.spiders.spiderobject.manager.abstracts;

import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;

/**
 * Manager command to manipulate spider
 * @author xianglong
 * @created 2015年7月23日 下午3:27:08
 * @version 1.0
 */
public class ManagerCommandBean{
	private Command cc;
	private String id;
	private AbstractSpiderObject parent;
	private AbstractSpiderObject childOrOther;
	
	public ManagerCommandBean(Command cc,String id){
		this.cc=cc;
		this.id=id;
	}
	public enum Command{
		START,
		STARTALL,
		STOP,
		FINISH,
		UPDATE,
		DELETE,
		ADDCHILD,
		ADD
	}
	public Command getCc() {
		return cc;
	}
	public void setCc(Command cc) {
		this.cc = cc;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public AbstractSpiderObject getChildOrOther() {
		return childOrOther;
	}
	public void setChildOrOther(AbstractSpiderObject childOrOther) {
		this.childOrOther = childOrOther;
	}
	public AbstractSpiderObject getParent() {
		return parent;
	}
	public void setParent(AbstractSpiderObject parent) {
		this.parent = parent;
	}
}

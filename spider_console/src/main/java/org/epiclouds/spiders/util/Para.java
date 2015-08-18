package org.epiclouds.spiders.util;

import java.lang.reflect.Method;


public class Para {
	private volatile StringBuilder name;
	private volatile String desc;
	private volatile boolean running;
	
	public String getName() {
		return name.toString();
	}
	public void setName(String name) {
		this.name=new StringBuilder(name);
		this.name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	public void setValue(Object value) throws Exception{
		Method m=ConsoleConfig.class.getMethod("set"+this.name, String.class);
		m.invoke(null, value);
	}
	
	public String getValue() throws Exception{
		Method m=ConsoleConfig.class.getMethod("get"+name);
		return m.invoke(null).toString();
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return name.equals(((Para)obj).name);
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name.toString();
	}
	
}

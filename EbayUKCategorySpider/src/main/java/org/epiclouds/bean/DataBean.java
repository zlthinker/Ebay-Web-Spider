package org.epiclouds.bean;


public class DataBean {
	private String id;
	private String name;
	private String catch_time;
	private TerapeakBean data;
	public DataBean(){}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCatch_time() {
		return catch_time;
	}
	public void setCatch_time(String catch_time) {
		this.catch_time = catch_time;
	}
	public TerapeakBean getData() {
		return data;
	}
	public void setData(TerapeakBean data) {
		this.data = data;
	}
	
}

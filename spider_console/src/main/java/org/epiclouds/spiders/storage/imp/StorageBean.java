package org.epiclouds.spiders.storage.imp;

import com.mongodb.DBObject;

public class StorageBean {
	private String dbstr;
	private String tablestr;
	private DBObject data;
	private DBObject condition;
	private OperationType type;
	public String getDbstr() {
		return dbstr;
	}
	public void setDbstr(String dbstr) {
		this.dbstr = dbstr;
	}
	public String getTablestr() {
		return tablestr;
	}
	public void setTablestr(String tablestr) {
		this.tablestr = tablestr;
	}
	public DBObject getData() {
		return data;
	}
	public void setData(DBObject data) {
		this.data = data;
	}
	public DBObject getCondition() {
		return condition;
	}
	public void setCondition(DBObject condition) {
		this.condition = condition;
	}
	
	public OperationType getType() {
		return type;
	}
	public void setType(OperationType type) {
		this.type = type;
	}

	public enum OperationType{
		INSERT,DELETE,UPDATE,UPORINSERT,FIND
	}
}

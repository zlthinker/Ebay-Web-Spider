package org.epiclouds.spiders.dbstorage.manager.abstracts;

import java.util.LinkedList;
import java.util.List;

import org.epiclouds.spiders.dbstorage.condition.abstracts.ConditionInterface;
import org.epiclouds.spiders.dbstorage.data.abstracts.DataEntryInterface;

/**
 * 
 * @author xianglong
 * @created 2015年6月5日 下午1:49:40
 * @version 1.0
 */
public class StorageBean {
	
	private String database;
	private String schema;
	private String table;
	@SuppressWarnings("rawtypes")
	private List<DataEntryInterface> datas;
	@SuppressWarnings("rawtypes")
	private List<ConditionInterface> conditons;
	private OperationType operationType;

	
	@SuppressWarnings("rawtypes")
	public List<DataEntryInterface> getDatas() {
		return datas;
	}
	public void setDatas(@SuppressWarnings("rawtypes") List<DataEntryInterface> datas) {
		this.datas = datas;
	}
	@SuppressWarnings("rawtypes")
	public List<ConditionInterface> getConditons() {
		return conditons;
	}
	public void setConditons(@SuppressWarnings("rawtypes") List<ConditionInterface> conditons) {
		this.conditons = conditons;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}
	public String getTable() {
		return table;
	}
	public String getDatabase() {
		return database;
	}
	public String getSchema() {
		return schema;
	}


	public OperationType getOperationType() {
		return operationType;
	}
	
	private StorageBean(Builder builder){
		table=builder.table;
		database=builder.database;
		schema=builder.schema;
		datas=builder.datas;
		this.conditons=builder.conditions;
		this.operationType=builder.operationType;
	}
	

	public enum OperationType{
		INSERT,DELETE,UPDATE,UPORINSERT,FIND
	}

	public static class Builder{
		private String table;
		private String database;
		private String schema;
		@SuppressWarnings("rawtypes")
		private List<DataEntryInterface> datas=new LinkedList<DataEntryInterface>();
		@SuppressWarnings("rawtypes")
		private List<ConditionInterface> conditions=new LinkedList<ConditionInterface>();
		private OperationType operationType;
		private Builder(OperationType operationType,String database,String table){
			this.operationType=operationType;
			this.database=database;
			this.table=table;
		}
		public static  Builder newBuilder(OperationType operationType,String database,String table){
			return new Builder(operationType,database,table);
		}
		public Builder setTable(String table){
			this.table=table;
			return this;
		}
		
		public Builder setDatabase(String database){
			this.database=database;
			return this;
		}
		
		public Builder addDataEntry(DataEntryInterface<?> data){
			this.datas.add(data);
			return this;
		}
		
		public StorageBean build(){
			return new StorageBean(this);
		}
		public Builder setSchema(String schema) {
			this.schema = schema;
			return this;
		}
		public Builder addConditon(ConditionInterface<?> condition) {
			this.conditions.add(condition);
			return this;
		}
	}
}

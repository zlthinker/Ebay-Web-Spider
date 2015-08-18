package org.epiclouds.spiders.dbstorage.abstracts;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.epiclouds.spiders.dbstorage.condition.abstracts.ConditionInterface;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.data.impl.DBDataEntry;
import org.epiclouds.spiders.dbstorage.manager.abstracts.*;
import org.epiclouds.spiders.dbstorage.manager.impl.MongoDBSyncManager;

import com.mongodb.DBObject;

/**
 * 
 * @author xianglong
 * @created 2015年6月5日 下午1:49:40
 * @version 1.0
 */
public class TestStorageBean {
	public static void main(String[] args) throws Exception{
		
		DBMangerInterface manager=new MongoDBSyncManager();
/*		executeInsert(manager);
		executeDelete(manager);
		executeUpdate(manager);
		executeUpdateOrInsert(manager);*/
		executeFind(manager);
	}
	/*public static void executeDelete(DBMangerInterface manager) throws Exception{
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.DELETE,"ebay","category_item").
				addConditon(new EqualCondition<String>
		("categoryId", "123")).build();
		manager.execute(sb);
	}
	public static void executeInsert(DBMangerInterface manager) throws Exception{
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.INSERT
				,"ebay","category_item").addDataEntry(new DBDataEntry
		<String>("categoryId", "123")).build();
		manager.execute(sb);
	}
	public static void executeUpdate(DBMangerInterface manager) throws Exception{
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.UPDATE
				,"ebay","category_item").
				addConditon(new EqualCondition<String>
		("categoryId", "123")).addDataEntry(new DBDataEntry
		<String>("categoryId", "456")).build();
		manager.execute(sb);
	}
	public static void executeUpdateOrInsert(DBMangerInterface manager) throws Exception{
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.UPORINSERT
				,"ebay","category_item").
				addConditon(new EqualCondition<String>
		("categoryId", "7887")).addDataEntry(new DBDataEntry
		<String>("categoryId", "7289238792389723789")).build();
		manager.execute(sb);
	}*/
	public static void executeFind(DBMangerInterface manager){
		StorageBean sb=StorageBean.Builder.newBuilder(StorageBean.OperationType.UPDATE
				,"ebay","category_item").build();
		/*List<DBObject> list=manager.find(sb);
		System.err.println(list.size());*/
	}
	
}

package org.epiclouds.spiders.dbstorage.manager.impl;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.epiclouds.spiders.config.manager.abstracts.AbstractConfigManager;
import org.epiclouds.spiders.dbstorage.condition.abstracts.ConditionInterface;
import org.epiclouds.spiders.dbstorage.data.abstracts.DataEntryInterface;
import org.epiclouds.spiders.dbstorage.data.impl.DBDataEntry;
import org.epiclouds.spiders.dbstorage.manager.abstracts.DBMangerInterface;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean.Builder;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * The mongo db sync manager
 * @author xianglong
 * @created 2015年6月5日 下午3:36:56
 * @version 1.0
 */
public  class MongoDBSyncManager implements DBMangerInterface{
	
	@Resource
	private AbstractConfigManager configManager;
	
	private MongoClient client;
	
	
	public  MongoDBSyncManager() throws UnknownHostException{
		
	}
	
	public void init() throws Exception{
		if(client==null){
			MongoCredential credential = MongoCredential.createCredential(
					configManager.getValue("mongo_user",String.class),
					configManager.getValue("mongo_authticateDatabase",String.class),
					configManager.getValue("mongo_pass",String.class).toCharArray());
			client = new MongoClient(new ServerAddress(configManager.getValue("mongo_host",String.class),
					configManager.getValue("mongo_port",Integer.class)), 
					Arrays.asList(credential));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void insert(StorageBean cond) {
		DBObject data=new BasicDBObject();
		for(DataEntryInterface<DBObject> ci:cond.getDatas()){
			ci.toLocalMongoValueObject(data);
		}
		client.getDB(cond.getDatabase()).getCollection(cond.getTable()).insert(data);
	}
	
	@SuppressWarnings("unchecked")
	protected void update(StorageBean cond) {
		DBObject condition=new BasicDBObject();
		for(ConditionInterface<DBObject> ci:cond.getConditons()){
			ci.toLocalMongoCondition(condition);
		}
		DBObject data=new BasicDBObject();
		for(DataEntryInterface<DBObject> ci:cond.getDatas()){
			ci.toLocalMongoValueObject(data);
		}
		client.getDB(cond.getDatabase()).getCollection(cond.getTable()).update(condition, data);
	}


	


	protected void delete(StorageBean cond) {
		// TODO Auto-generated method stub
		DBObject condition=new BasicDBObject();
		for(ConditionInterface ci:cond.getConditons()){
			ci.toLocalMongoCondition(condition);
		}
		client.getDB(cond.getDatabase()).getCollection(cond.getTable()).remove(condition);
	}


	protected void updateOrInsert(StorageBean sb) {
		DBObject condition=new BasicDBObject();
		for(ConditionInterface ci:sb.getConditons()){
			ci.toLocalMongoCondition(condition);
		}
		DBObject data=new BasicDBObject();
		for(DataEntryInterface<DBObject> ci:sb.getDatas()){
			ci.toLocalMongoValueObject(data);
		}
		client.getDB(sb.getDatabase()).getCollection(sb.getTable()).update(condition, data,true,false);
	}


	protected void execute(StorageBean sb) throws Exception {
		if(sb!=null){
			switch (sb.getOperationType()) {
			case DELETE:
				delete(sb);
				break;
			case INSERT:
				insert(sb);
				break;		
			case UPDATE:
				update(sb);
				break;
			case UPORINSERT:
				updateOrInsert(sb);
				break;
			default:
				break;
			}
			
		}
	}


	public<T> LinkedList<T> find(StorageBean sb,Class<T> cls) throws Exception {
		DBObject condition=new BasicDBObject();
		for(ConditionInterface<DBObject> ci:sb.getConditons()){
			ci.toLocalMongoCondition(condition);
		}
		DBCursor dbcursor=client.getDB(sb.getDatabase()).getCollection(sb.getTable()).find(condition);
		LinkedList<T> resultList= new LinkedList<T>();
		while(dbcursor.hasNext()){
			 DBObject dbo=(dbcursor.next());
			 resultList.add(JSONObject.parseObject(dbo.toString(),cls));
		}
		return resultList;
	}

	@Override
	public void storage(Builder builder, Object data) throws Exception {
		if(data!=null){
			JSONObject o=JSONObject.parseObject(JSONObject.toJSONString(data));
			for(Entry<String, Object> entry:o.entrySet()){
				builder.addDataEntry(new DBDataEntry<Object>(entry.getKey(),entry.getValue()));
			}
		}
		StorageBean sb=builder.build();
		execute(sb);
	}


	
}

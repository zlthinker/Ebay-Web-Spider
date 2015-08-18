package org.epiclouds.spiders.dbstorage.condition.abstracts;

import com.mongodb.DBObject;


/**
 * the condition interface
 * get the local condition type 
 * @author xianglong
 * @created 2015年6月5日 下午2:10:41
 * @version 1.0
 * @param <T>
 */
public interface ConditionInterface <T>{
	public DBObject toLocalMongoCondition(DBObject k);
	public<K> K toLocalRelationDBCondition(K k);
}

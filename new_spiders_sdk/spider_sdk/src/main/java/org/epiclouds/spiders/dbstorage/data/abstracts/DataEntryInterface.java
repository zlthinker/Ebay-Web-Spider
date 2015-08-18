package org.epiclouds.spiders.dbstorage.data.abstracts;

import com.mongodb.DBObject;


/**
 * the data entry
 * @author xianglong
 * @created 2015年6月5日 下午2:10:41
 * @version 1.0
 * @param <T>
 */
public interface DataEntryInterface <T>{
	public DBObject toLocalMongoValueObject(DBObject t);
	public Object toLocalRDBValueObject(Object t);
}

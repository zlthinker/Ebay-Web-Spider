package org.epiclouds.spiders.dbstorage.data.impl;



import org.epiclouds.spiders.dbstorage.data.abstracts.AbstractDataEntry;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;

/**
 * The mongo equal condition
 * @author xianglong
 * @created 2015年6月5日 下午2:20:37
 * @version 1.0
 * @param <DBObject>
 */
public class DBDataEntry<T> extends AbstractDataEntry<T>{

	public DBDataEntry(String name, T value) {
		super(name, value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public DBObject toLocalMongoValueObject(DBObject t) {
		t.put(name, value);
		return t;
	}

	@Override
	public Object toLocalRDBValueObject(Object t) {
		// TODO Auto-generated method stub
		return null;
	}

}

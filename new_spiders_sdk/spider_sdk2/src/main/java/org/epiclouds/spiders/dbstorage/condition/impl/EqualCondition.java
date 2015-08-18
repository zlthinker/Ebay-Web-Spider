package org.epiclouds.spiders.dbstorage.condition.impl;



import org.epiclouds.spiders.dbstorage.condition.abstracts.AbstractCondition;

import com.mongodb.DBObject;

/**
 * The mongo equal condition
 * @author xianglong
 * @created 2015年6月5日 下午2:20:37
 * @version 1.0
 * @param <DBObject>
 */
public class EqualCondition<T> extends AbstractCondition<T>{

	public EqualCondition(String name, T value) {
		super(name, value);
	}



	@Override
	public DBObject toLocalMongoCondition(DBObject t) {
		t.put(name, value);
		return t;
	}
	




	@Override
	public Object toLocalRelationDBCondition(Object k) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

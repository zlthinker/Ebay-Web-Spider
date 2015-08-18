package org.epiclouds.spiders.dbstorage.condition.abstracts;


/**
 * the condition abstract
 * @author xianglong
 * @created 2015年6月5日 下午2:11:49
 * @version 1.0
 */
public abstract class AbstractCondition<T> implements ConditionInterface<T>{
	protected String name;
	
	protected T value;
	
	public AbstractCondition(String name,T value){
		this.name=name;
		this.value=value;
	}

}

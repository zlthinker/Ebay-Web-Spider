package org.epiclouds.spiders.dbstorage.data.abstracts;


/**
 * the data entry abstract
 * @author xianglong
 * @created 2015年6月5日 下午2:11:49
 * @version 1.0
 */
public abstract class AbstractDataEntry<T> implements DataEntryInterface<T>{
	protected String name;
	
	protected T value;
	
	public AbstractDataEntry(String name,T value){
		this.name=name;
		this.value=value;
	}

}

package org.epiclouds.spiders.config.field.abstracts;

/**
 * spider config operations
 * @author xianglong
 * @created 2015年6月2日 下午4:18:39
 * @version 1.0
 */
public interface FieldInterface<T> {
	/**
	 * 
	 * @param name
	 * @return the value
	 */
	public T getValue();
	public void setValue(String value);
}

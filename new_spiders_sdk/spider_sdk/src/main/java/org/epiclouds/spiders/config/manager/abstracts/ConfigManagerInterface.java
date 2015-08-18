package org.epiclouds.spiders.config.manager.abstracts;

import java.util.List;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;

/**
 * 
 * @author xianglong
 * @created 2015年6月2日 下午6:47:40
 * @version 1.0
 */

public interface ConfigManagerInterface {
	public AbstractField get(String name);
	public<T> T getValue(String name,Class<T> cls);
	public void set(String name,String value);
	public void register(AbstractField field);
	public List<AbstractField> getAllFields();
	
}

package org.epiclouds.spiders.config.manager.abstracts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;
import org.springframework.stereotype.Component;

/**
 * 
 * @author xianglong
 * @created 2015年6月2日 下午7:48:54
 * @version 1.0
 */
@Component
public abstract class AbstractConfigManager implements ConfigManagerInterface{
	/**
	 * the fields
	 */
	protected final  Map<String,AbstractField> fields=new ConcurrentHashMap<String,AbstractField>();
	
	
	@Override
	public AbstractField get(String name) {
		// TODO Auto-generated method stub
		return fields.get(name);
	}

	@Override
	public void set(String name, String value) {
		// TODO Auto-generated method stub
		AbstractField field=fields.get(name);
		if(field==null) return;
		field.setValue(value);
	}

	@Override
	public void register(AbstractField field) {
		if(field==null) return;
		fields.put(field.getName(), field);
	}

	@Override
	public List<AbstractField> getAllFields() {
		// TODO Auto-generated method stub
		List<AbstractField> list=new LinkedList<AbstractField>();
		list.addAll(fields.values());
		return list;
	}

	@Override
	public<T> T getValue(String name,Class<T> cls) {
		AbstractField field=fields.get(name);
		if(field==null) return null;
		return (T) field.getValue();
	}
	
}

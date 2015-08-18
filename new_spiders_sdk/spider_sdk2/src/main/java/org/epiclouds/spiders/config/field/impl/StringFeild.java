package org.epiclouds.spiders.config.field.impl;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;

/**
 * 
 * @author xianglong
 * @created 2015年6月2日 下午4:22:00
 * @version 1.0
 */
public class StringFeild<T extends String> extends AbstractField<String>{

	public StringFeild(String name,String value, String desc, boolean running) {
		super(name,value, desc, running);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		this.value=value;
	}
	
}

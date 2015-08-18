package org.epiclouds.spiders.config.field.impl;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;

public class LongFeild<T extends Long>  extends AbstractField<Long>{

	public LongFeild(String name,String value, String desc, boolean running) {
		super(name,value, desc, running);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		this.value=Long.parseLong(value);
	}
	
}

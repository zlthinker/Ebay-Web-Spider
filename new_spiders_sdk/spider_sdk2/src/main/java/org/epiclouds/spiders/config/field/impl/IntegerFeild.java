package org.epiclouds.spiders.config.field.impl;

import org.epiclouds.spiders.config.field.abstracts.AbstractField;

public class IntegerFeild<T extends Integer> extends AbstractField<Integer>{
	
	public IntegerFeild(String name,String value,String desc, boolean running) {
		super(name,value, desc, running);
	}

	@Override
	public void setValue(String value) {
		// TODO Auto-generated method stub
		this.value=Integer.parseInt(value);
	}

}

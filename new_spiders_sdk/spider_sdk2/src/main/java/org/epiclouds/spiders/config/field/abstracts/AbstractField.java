package org.epiclouds.spiders.config.field.abstracts;

import org.epiclouds.spiders.config.field.impl.IntegerFeild;
import org.epiclouds.spiders.config.field.impl.LongFeild;
import org.epiclouds.spiders.config.field.impl.StringFeild;

public abstract class AbstractField<T> implements FieldInterface{
	private final String name;
	protected volatile T value;
	private final String desc;
	private final boolean isrun;
	public AbstractField(String name,String value,String desc,boolean running){
		this.name=name;
		this.desc=desc;
		this.isrun=running;
		setValue(value);
	}

	@Override
	public T getValue() {
		// TODO Auto-generated method stub
		return value;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public boolean isRunning() {
		return isrun;
	}


	public boolean isIsrun() {
		return isrun;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public static AbstractField buildFeild(String name,String value,String desc,boolean running,int type){
		AbstractField af=null;
		switch(type){
			case 0: {
				af=new StringFeild(name,value, desc, running);
				break;
			}
			case 1:{
				af=new IntegerFeild(name,value, desc, running);
				break;
			}
			case 2:{
				af=new LongFeild(name,value, desc, running);
				break;
			}
			default:{
				af=new StringFeild(name,value, desc, running);
			}
			
		}
		return af;
	}

	
	
}

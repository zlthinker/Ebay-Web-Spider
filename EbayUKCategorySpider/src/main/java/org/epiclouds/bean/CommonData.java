package org.epiclouds.bean;

import java.util.LinkedList;
import java.util.List;

public class CommonData {
	private volatile String name;
	private volatile List<Object[]> data=new LinkedList<Object[]>();
	private volatile Double minValue;
	private volatile Double maxValue;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Object[]> getData() {
		return data;
	}
	public void setData(List<Object[]> data) {
		this.data = data;
	}
	public Double getMinValue() {
		return minValue;
	}
	public void setMinValue(Double minValue) {
		this.minValue = minValue;
	}
	public Double getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
	}
	
}

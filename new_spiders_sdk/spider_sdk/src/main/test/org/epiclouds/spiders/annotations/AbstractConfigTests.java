package org.epiclouds.spiders.annotations;

public class AbstractConfigTests {
	@SpiderFeildConfig(desc="名称！")
	private String name11;

	public String getName11() {
		return name11;
	}

	public void setName11(String name11) {
		this.name11 = name11;
	}
	
	
}

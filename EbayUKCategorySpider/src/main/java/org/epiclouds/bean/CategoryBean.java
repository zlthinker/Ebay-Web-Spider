package org.epiclouds.bean;


import java.util.List;

/**
 * @author zl
 *
 */
public class CategoryBean {
	private String id;
	private String parentid;
	private String name;
	private boolean isleaf;
	private List<String> children;
	

	public CategoryBean() {
		
	}
	
	public CategoryBean(String id, String parentId, String name,
			boolean isLeaf, List<String> children) {
		this.id = id;
		this.parentid = parentId;
		this.name = name;
		this.isleaf = isLeaf;
		this.children = children;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public List<String> getChildren() {
		return children;
	}

	public void setChildren(List<String> children) {
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public boolean isIsleaf() {
		return isleaf;
	}

	public void setIsleaf(boolean isleaf) {
		this.isleaf = isleaf;
	}

	public void setId(String id) {
		this.id = id;
	}
}

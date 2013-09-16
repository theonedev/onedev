package com.pmease.gitop.web;

import java.io.Serializable;
import java.util.List;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class Bean implements Serializable {
	
	private String name;
	
	private boolean married;
	
	private List<ChildBean> childs;
	
	private int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMarried() {
		return married;
	}

	public void setMarried(boolean married) {
		this.married = married;
	}

	@Editable
	public List<ChildBean> getChilds() {
		return childs;
	}

	public void setChilds(List<ChildBean> childs) {
		this.childs = childs;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
	
}

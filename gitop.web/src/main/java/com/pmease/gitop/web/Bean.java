package com.pmease.gitop.web;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.Validatable;
import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class Bean implements Serializable, Validatable {
	
	private String name;
	
	private boolean married;
	
	private List<ChildBean> childs;
	
	private ChildBean child;
	
	private Integer age;

	@Editable(description="This is something interesting.")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable
	public boolean isMarried() {
		return married;
	}

	public void setMarried(boolean married) {
		this.married = married;
	}

	@Editable
	@Size(min=5)
	public List<ChildBean> getChilds() {
		return childs;
	}

	public void setChilds(List<ChildBean> childs) {
		this.childs = childs;
	}

	@Editable
	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Editable
	public ChildBean getChild() {
		return child;
	}

	public void setChild(ChildBean child) {
		this.child = child;
	}

	@Override
	public void validate(EditContext editContext) {
		editContext.error("tananade");
	}
	
}

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
public class Bean implements Serializable, Validatable<Void> {
	
	private String name;
	
	private boolean married;
	
	private List<ChildBean> childs;
	
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

	@Override
	public void validate(EditContext<Void> editContext) {
		editContext.error("tananade");
	}
	
}

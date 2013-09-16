package com.pmease.gitop.web;

import java.io.Serializable;
import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.ErrorContext;
import com.pmease.commons.editable.Validatable;
import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class ChildBean implements Serializable, Validatable {
	private String childName;
	
	private Boolean childMarried;

	@Editable
	@NotEmpty
	public String getChildName() {
		return childName;
	}

	public void setChildName(String childName) {
		this.childName = childName;
	}

	@Editable
	public Boolean isChildMarried() {
		return childMarried;
	}

	public void setChildMarried(Boolean childMarried) {
		this.childMarried = childMarried;
	}

	@Override
	public void validate(Set<String> propertyNames, ErrorContext errorContext) {
		if (propertyNames.contains("childName")) {
			if (childName.startsWith("child") && childMarried) {
				errorContext.error("childName", "child can not marry.");
				errorContext.error("childMarried", "child can not marry.");
			}
		}
	}
	
}

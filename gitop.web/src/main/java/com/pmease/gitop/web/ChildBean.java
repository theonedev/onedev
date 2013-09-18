package com.pmease.gitop.web;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.Validatable;
import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public abstract class ChildBean implements Serializable, Validatable {
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
	public void validate(EditContext editContext) {
		if (!editContext.hasError("childName", true)) {
			if (childName.startsWith("child") && childMarried) {
				editContext.error("child can not marry.");
				editContext.getChildContext("childMarried").error("child can not marry.");
			}
		}
	}
	
}

package io.onedev.server.model.support.administration.jobexecutor;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class NodeSelectorEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String labelName;
	
	private String labelValue;

	@Editable(order=100)
	@NotEmpty
	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	@Editable(order=200)
	@NotEmpty
	public String getLabelValue() {
		return labelValue;
	}

	public void setLabelValue(String labelValue) {
		this.labelValue = labelValue;
	}
	
}
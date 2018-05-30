package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class FieldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private InputSpec field;

	@Editable(name="Type")
	@NotNull(message="may not be empty")
	public InputSpec getField() {
		return field;
	}

	public void setField(InputSpec field) {
		this.field = field;
	}
	
}

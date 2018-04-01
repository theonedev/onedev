package io.onedev.server.web.page.project.setting.issueworkflow.fields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.inputspec.InputSpec;

@Editable
public class FieldSpecBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private InputSpec fieldSpec;

	@Editable(name="Type")
	@NotNull
	public InputSpec getFieldSpec() {
		return fieldSpec;
	}

	public void setFieldSpec(InputSpec fieldSpec) {
		this.fieldSpec = fieldSpec;
	}
	
}

package io.onedev.server.web.page.project.setting.issuecustomfields;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.input.Input;

@Editable
public class FieldSpecBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Input fieldSpec;

	@Editable(name="Type")
	@NotNull
	public Input getFieldSpec() {
		return fieldSpec;
	}

	public void setFieldSpec(Input fieldSpec) {
		this.fieldSpec = fieldSpec;
	}
	
}

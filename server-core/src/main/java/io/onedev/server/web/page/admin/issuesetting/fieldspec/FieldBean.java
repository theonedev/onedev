package io.onedev.server.web.page.admin.issuesetting.fieldspec;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.annotation.Editable;

@Editable
public class FieldBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private FieldSpec field;

	@Editable(name="Type", order=100)
	@NotNull(message="may not be empty")
	public FieldSpec getField() {
		return field;
	}

	public void setField(FieldSpec field) {
		this.field = field;
	}

}

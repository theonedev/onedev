package io.onedev.server.web.editable.issue.fieldinstance;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class FieldListCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public FieldListCssResourceReference() {
		super(FieldListCssResourceReference.class, "field-list.css");
	}

}

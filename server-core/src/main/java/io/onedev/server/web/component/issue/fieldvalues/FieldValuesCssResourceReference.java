package io.onedev.server.web.component.issue.fieldvalues;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class FieldValuesCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public FieldValuesCssResourceReference() {
		super(FieldValuesCssResourceReference.class, "field-values.css");
	}

}

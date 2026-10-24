package io.onedev.server.web.asset.fieldcompare;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class FieldCompareCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public FieldCompareCssResourceReference() {
		super(FieldCompareCssResourceReference.class, "field-compare.css");
	}

}

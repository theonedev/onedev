package io.onedev.server.web.editable.buildspec.imports;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class ImportCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ImportCssResourceReference() {
		super(ImportCssResourceReference.class, "import.css");
	}

}

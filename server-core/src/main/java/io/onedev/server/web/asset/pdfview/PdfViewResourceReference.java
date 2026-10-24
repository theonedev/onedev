package io.onedev.server.web.asset.pdfview;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PdfViewResourceReference extends BaseDependentResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public PdfViewResourceReference() {
		super(PdfViewResourceReference.class, "pdf-view.js");
	}

}

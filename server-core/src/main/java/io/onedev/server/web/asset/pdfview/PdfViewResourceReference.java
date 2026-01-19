package io.onedev.server.web.asset.pdfview;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PdfViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PdfViewResourceReference() {
		super(PdfViewResourceReference.class, "pdf-view.js");
	}

}

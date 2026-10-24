package io.onedev.server.web.component.diff.blob.title;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class DiffTitleResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public DiffTitleResourceReference() {
		super(DiffTitleResourceReference.class, "diff-title.css");
	}

}

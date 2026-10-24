package io.onedev.server.web.page.project.blob.search.advanced;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class AdvancedSearchResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public AdvancedSearchResourceReference() {
		super(AdvancedSearchResourceReference.class, "advanced-search.css");
	}

}

package io.onedev.server.web.behavior.sortable;

import org.apache.wicket.request.resource.CssResourceReference;

public class SortableCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public SortableCssResourceReference() {
		super(SortableCssResourceReference.class, "sortable.css");
	}

}

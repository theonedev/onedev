package com.gitplex.server.web.component.revisionpicker;

import org.apache.wicket.request.resource.CssResourceReference;

public class RevisionPickerResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public RevisionPickerResourceReference() {
		super(RevisionPickerResourceReference.class, "revision-picker.css");
	}

}

package com.turbodev.server.web.component.branchpicker;

import org.apache.wicket.request.resource.CssResourceReference;

public class BranchPickerResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public BranchPickerResourceReference() {
		super(BranchPickerResourceReference.class, "branch-picker.css");
	}

}

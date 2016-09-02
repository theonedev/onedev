package com.pmease.commons.wicket.component.loadingindicator;

import org.apache.wicket.request.resource.CssResourceReference;

public class LoadingIndicatorResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public LoadingIndicatorResourceReference() {
		super(LoadingIndicatorResourceReference.class, "loading-indicator.css");
	}

}

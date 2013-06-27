package com.pmease.commons.web.behavior.modal;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ModalResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public ModalResourceReference() {
		super(ModalBehavior.class, "modal.js");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				JavaScriptHeaderItem.forReference(org.apache.wicket.bootstrap.Bootstrap.responsive()),
				CssHeaderItem.forReference(new CssResourceReference(ModalBehavior.class, "modal.css")));
	}

}

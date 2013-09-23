package com.pmease.commons.wicket.behavior.modal;

import java.util.Arrays;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class ModalResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static ModalResourceReference get() {
		return INSTANCE;
	}
	
	private static ModalResourceReference INSTANCE = new ModalResourceReference();
	
	private ModalResourceReference() {
		super(ModalBehavior.class, "modal.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Arrays.asList(
				BootstrapHeaderItem.get(),
				CssHeaderItem.forReference(new CssResourceReference(ModalBehavior.class, "modal.css")));
	}

}

package com.pmease.commons.wicket.editable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.util.EasyList;
import com.pmease.commons.wicket.asset.bootstrap.BootstrapHeaderItem;

public class EditableResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static EditableResourceReference get() {
		return INSTANCE;
	}
	
	private static EditableResourceReference INSTANCE = new EditableResourceReference();
	
	private EditableResourceReference() {
		super(EditableResourceReference.class, "editable.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return EasyList.of(
				BootstrapHeaderItem.get(),
				CssHeaderItem.forReference(new CssResourceReference(EditableResourceReference.class, "editable.css")));
	}

}

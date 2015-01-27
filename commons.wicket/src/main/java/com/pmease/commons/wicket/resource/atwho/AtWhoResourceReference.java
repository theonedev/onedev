package com.pmease.commons.wicket.resource.atwho;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.resource.caret.CaretResourceReference;

public class AtWhoResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AtWhoResourceReference INSTANCE = new AtWhoResourceReference();
	
	private AtWhoResourceReference() {
		super(AtWhoResourceReference.class, "jquery.atwho.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), ImmutableList.<HeaderItem>of(
				JavaScriptHeaderItem.forReference(CaretResourceReference.INSTANCE),
				CssHeaderItem.forReference(new CssResourceReference(AtWhoResourceReference.class, "jquery.atwho.css"))));
	}

}

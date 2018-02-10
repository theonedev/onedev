package com.turbodev.server.web.assets.js.align;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.turbodev.server.web.assets.js.textareacaretposition.TextareaCaretPositionResourceReference;

public class AlignResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public AlignResourceReference() {
		super(AlignResourceReference.class, "jquery.align.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(
				Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TextareaCaretPositionResourceReference()));
		return dependencies;
	}
	
}

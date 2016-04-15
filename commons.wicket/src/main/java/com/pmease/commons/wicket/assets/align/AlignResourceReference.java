package com.pmease.commons.wicket.assets.align;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.textareacaretposition.TextareaCaretPositionResourceReference;

public class AlignResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AlignResourceReference INSTANCE = new AlignResourceReference();
	
	private AlignResourceReference() {
		super(AlignResourceReference.class, "jquery.align.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(TextareaCaretPositionResourceReference.INSTANCE));
		return dependencies;
	}
	
}

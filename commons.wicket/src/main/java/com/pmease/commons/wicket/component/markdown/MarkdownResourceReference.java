package com.pmease.commons.wicket.component.markdown;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.codemirror.HighlightResourceReference;

@SuppressWarnings("serial")
public class MarkdownResourceReference extends JavaScriptResourceReference {

	public static final MarkdownResourceReference INSTANCE = new MarkdownResourceReference();
	
	private MarkdownResourceReference() {
		super(MarkdownResourceReference.class, "markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(HighlightResourceReference.INSTANCE));		
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownPanel.class, "markdown.css")));
		return dependencies;
	}

}

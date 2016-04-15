package com.pmease.commons.wicket.component.markdownviewer;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.codemirror.HighlightResourceReference;

@SuppressWarnings("serial")
public class MarkdownViewerResourceReference extends JavaScriptResourceReference {

	public static final MarkdownViewerResourceReference INSTANCE = new MarkdownViewerResourceReference();
	
	private MarkdownViewerResourceReference() {
		super(MarkdownViewerResourceReference.class, "markdown-viewer.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(HighlightResourceReference.INSTANCE));		
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownViewer.class, "markdown-viewer.css")));
		return dependencies;
	}

}

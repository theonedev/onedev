package com.gitplex.commons.wicket.component.markdown;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.gitplex.commons.wicket.page.CommonDependentResourceReference;

public class MarkdownResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MarkdownResourceReference() {
		super(MarkdownResourceReference.class, "markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));		
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownResourceReference.class, "markdown.css")));
		return dependencies;
	}

}

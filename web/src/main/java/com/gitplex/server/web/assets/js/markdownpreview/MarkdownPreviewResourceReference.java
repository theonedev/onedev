package com.gitplex.server.web.assets.js.markdownpreview;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.js.codemirror.CodeMirrorResourceReference;
import com.gitplex.server.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class MarkdownPreviewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MarkdownPreviewResourceReference() {
		super(MarkdownPreviewResourceReference.class, "markdown-preview.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				MarkdownPreviewResourceReference.class, "markdown-preview.css")));
		return dependencies;
	}

}

package com.pmease.commons.wicket.behavior.markdown;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.atwho.AtWhoResourceReference;
import com.pmease.commons.wicket.assets.caret.CaretResourceReference;
import com.pmease.commons.wicket.assets.codemirror.CodeMirrorResourceReference;
import com.pmease.commons.wicket.assets.hotkeys.HotkeysResourceReference;
import com.pmease.commons.wicket.page.CommonDependentResourceReference;

public class MarkdownResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public MarkdownResourceReference() {
		super(MarkdownResourceReference.class, "markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AtWhoResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(MarkdownResourceReference.class, "bootstrap-markdown.js")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(MarkdownResourceReference.class, "bootstrap-markdown.min.css")));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(MarkdownResourceReference.class, "markdown.css")));
		return dependencies;
	}

}

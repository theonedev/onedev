package io.onedev.server.web.editable.code;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CodeSupportResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeSupportResourceReference() {
		super(CodeSupportResourceReference.class, "code-support.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(CodeSupportResourceReference.class, "code-support.css")));
		return dependencies;
	}

}

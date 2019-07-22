package io.onedev.server.web.editable.script;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class ScriptSupportResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ScriptSupportResourceReference() {
		super(ScriptSupportResourceReference.class, "script-support.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(ScriptSupportResourceReference.class, "script-support.css")));
		return dependencies;
	}

}

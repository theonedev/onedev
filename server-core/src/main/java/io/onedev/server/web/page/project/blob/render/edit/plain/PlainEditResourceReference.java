package io.onedev.server.web.page.project.blob.render.edit.plain;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PlainEditResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PlainEditResourceReference() {
		super(PlainEditResourceReference.class, "plain-edit.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(PlainEditResourceReference.class, "plain-edit.css")));
		return dependencies;
	}

}

package io.onedev.server.web.asset.katex;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.resourcebundle.ResourceBundle;

@ResourceBundle
public class KatexResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public KatexResourceReference() {
		super(KatexResourceReference.class, "katex.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(KatexResourceReference.class, "katex.min.css")));
		return dependencies;
	}

}

package io.onedev.server.web.asset.codemirror;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

public class CodeThemeCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CodeThemeCssResourceReference() {
		super(CodeThemeCssResourceReference.class, "theme-custom.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				CodeThemeCssResourceReference.class, "theme/eclipse.css")));
		return dependencies;
	}

}

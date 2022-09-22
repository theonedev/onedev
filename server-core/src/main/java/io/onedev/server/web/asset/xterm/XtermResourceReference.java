package io.onedev.server.web.asset.xterm;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class XtermResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;
	
	public XtermResourceReference() {
		super(XtermResourceReference.class, "xterm.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new BaseDependentResourceReference(
				XtermResourceReference.class, "xterm-addon-fit.js")));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				XtermResourceReference.class, "xterm.css")));
		return dependencies;
	}

}

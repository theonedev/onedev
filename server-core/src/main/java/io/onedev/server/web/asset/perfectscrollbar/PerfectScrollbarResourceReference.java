package io.onedev.server.web.asset.perfectscrollbar;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class PerfectScrollbarResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public PerfectScrollbarResourceReference() {
		super(PerfectScrollbarResourceReference.class, "perfect-scrollbar.min.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				PerfectScrollbarResourceReference.class, "perfect-scrollbar.css")));
		return dependencies;
	}

}

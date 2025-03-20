package io.onedev.server.web.asset.flatpickr;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class FlatPickrResourceReference extends BaseDependentResourceReference {
	
	private static final long serialVersionUID = 1L;
	
	public FlatPickrResourceReference() {
		super(FlatPickrResourceReference.class, "flatpickr.min.js");
	}
	
	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(FlatPickrResourceReference.class, "flatpickr.min.css")
		));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(FlatPickrResourceReference.class, "dark.css")
		));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FlatPickrResourceReference.class, "flatpickr-locale.js")
		));
		
		return dependencies;
	}
}

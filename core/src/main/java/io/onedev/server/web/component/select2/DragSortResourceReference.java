package io.onedev.server.web.component.select2;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.asset.jqueryui.JQueryUIResourceReference;

public class DragSortResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public DragSortResourceReference() {
		super(DragSortResourceReference.class, "res/drag-sort.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		return dependencies;
	}

}

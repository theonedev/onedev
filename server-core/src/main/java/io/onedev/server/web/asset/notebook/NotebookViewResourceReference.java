package io.onedev.server.web.asset.notebook;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import io.onedev.server.web.asset.marked.MarkedResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class NotebookViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public NotebookViewResourceReference() {
		super(NotebookViewResourceReference.class, "notebook-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(
			NotebookViewResourceReference.class, "notebook.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new MarkedResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				NotebookViewResourceReference.class, "notebook-view.css")));
		return dependencies;
	}

}

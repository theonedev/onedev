package io.onedev.server.web.component.savedquery;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.component.floating.FloatingResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class SavedQueriesResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SavedQueriesResourceReference() {
		super(SavedQueriesResourceReference.class, "saved-queries.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new FloatingResourceReference()));
		return dependencies;
	}

}

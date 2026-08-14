package io.onedev.server.web.component.savedquery;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;

public abstract class SavedQueriesLink extends AjaxLink<Void> {

	public SavedQueriesLink(String id) {
		super(id);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(SavedQueriesDropdownListener.show());
	}

}

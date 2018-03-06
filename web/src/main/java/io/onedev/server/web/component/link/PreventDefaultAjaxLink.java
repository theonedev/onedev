package io.onedev.server.web.component.link;

import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

public abstract class PreventDefaultAjaxLink<T> extends AjaxLink<T> {

	private static final long serialVersionUID = 1L;

	public PreventDefaultAjaxLink(String id) {
		super(id);
	}

	public PreventDefaultAjaxLink(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.setPreventDefault(true);
	}

}

package com.pmease.commons.wicket.component.clearable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class ClearableTextField<T> extends TextField<T> {

	public ClearableTextField(final String id) {
		super(id);
	}

	public ClearableTextField(final String id, final Class<T> type) {
		super(id, type);
	}

	public ClearableTextField(final String id, final IModel<T> model) {
		super(id, model);
	}

	public ClearableTextField(final String id, final IModel<T> model, final Class<T> type) {
		super(id, model, type);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		String script = String.format("pmease.commons.setupClearableInput('%s');", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}

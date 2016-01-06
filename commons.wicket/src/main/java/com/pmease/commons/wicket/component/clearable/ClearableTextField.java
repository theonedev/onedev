package com.pmease.commons.wicket.component.clearable;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.assets.clearable.ClearableResourceReference;

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
		
		response.render(JavaScriptHeaderItem.forReference(ClearableResourceReference.INSTANCE));
		String script = String.format("$('#%s').clearable();", getMarkupId(true));
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
}

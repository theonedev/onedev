package com.pmease.commons.wicket.component.select2;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class Select2Choice<T> extends com.vaynberg.wicket.select2.Select2Choice<T> {
	
	public Select2Choice(String id, IModel<T> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
	}

	public Select2Choice(String id, IModel<T> model) {
		super(id, model);
	}

	public Select2Choice(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(Select2Choice.class, "select2-bootstrap.css")));
	}
}

package com.pmease.commons.wicket.component.select2;

import java.util.Collection;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class Select2MultiChoice<T> extends com.vaynberg.wicket.select2.Select2MultiChoice<T> {

	public Select2MultiChoice(String id, IModel<Collection<T>> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
	}

	public Select2MultiChoice(String id, IModel<Collection<T>> model) {
		super(id, model);
	}

	public Select2MultiChoice(String id) {
		super(id);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(CssHeaderItem.forReference(new CssResourceReference(
				Select2MultiChoice.class, "select2-bootstrap.css")));
	}
}

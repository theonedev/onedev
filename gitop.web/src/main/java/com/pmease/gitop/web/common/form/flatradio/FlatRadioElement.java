package com.pmease.gitop.web.common.form.flatradio;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.form.radio.RadioButtonElement;

public class FlatRadioElement<T> extends RadioButtonElement<T> {

	private static final long serialVersionUID = 1L;

	private IModel<String> descriptionModel;
	
	public FlatRadioElement(String id, IModel<T> model, IModel<String> description) {
		super(id, model, "");
		this.descriptionModel = description;
	}

	@SuppressWarnings("serial")
	@Override
	protected Component createInputComponent(String id) {
		radioContainer = new FlatRadioElementPanel(id);
		radioContainer.add(radio = createRadio("radio"));
		radioContainer.add(new Label("description", descriptionModel));
		radioContainer.add(new FlatRadioBehavior());
		radioContainer.add(new AbstractDefaultAjaxBehavior() {

			@Override
			protected void respond(AjaxRequestTarget target) {
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				super.renderHead(component, response);
				response.render(OnDomReadyHeaderItem.forScript(String.format("$('#%s').radio()", radio.getMarkupId(true))));
			}
	    });
		return radioContainer;
	}
	
	protected Radio<T> createRadio(String id) {
		return new Radio<T>(id, radioModel);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (descriptionModel != null) {
			descriptionModel.detach();
		}
	}
}

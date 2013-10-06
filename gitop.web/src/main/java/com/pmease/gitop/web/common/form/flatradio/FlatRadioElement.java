package com.pmease.gitop.web.common.form.flatradio;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.pmease.gitop.web.common.form.radio.RadioButtonElement;

public class FlatRadioElement<T> extends RadioButtonElement<T> {

	private static final long serialVersionUID = 1L;

	private IModel<String> descriptionModel;
	
	public FlatRadioElement(String id, IModel<T> model, String description) {
		super(id, model, "");
		this.descriptionModel = Model.of(description);
	}

	@Override
	protected Component createInputComponent(String id) {
		radioContainer = new FlatRadioElementPanel(id);
		radioContainer.add(radio = createRadio("radio"));
		radioContainer.add(new Label("description", descriptionModel));
		radioContainer.add(new FlatRadioBehavior());
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

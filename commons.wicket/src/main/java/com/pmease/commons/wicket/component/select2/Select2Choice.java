package com.pmease.commons.wicket.component.select2;

import org.apache.wicket.model.IModel;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class Select2Choice<T> extends com.vaynberg.wicket.select2.Select2Choice<T> {
	
	public Select2Choice(String id, IModel<T> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
		
		add(new Select2BootstrapResourceBehavior());
	}

	public Select2Choice(String id, IModel<T> model) {
		this(id, model, null);
	}

	public Select2Choice(String id) {
		this(id, null);
	}

}

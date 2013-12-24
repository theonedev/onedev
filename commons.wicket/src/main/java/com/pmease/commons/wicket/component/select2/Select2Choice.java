package com.pmease.commons.wicket.component.select2;

import org.apache.wicket.model.IModel;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class Select2Choice<T> extends com.vaynberg.wicket.select2.Select2Choice<T> {

	public Select2Choice(String id) {
		super(id);
		
		add(new BootstrapFlavor());
	}

	public Select2Choice(String id, IModel<T> model, ChoiceProvider<T> provider) {
		super(id, model, provider);

		add(new BootstrapFlavor());
	}

	public Select2Choice(String id, IModel<T> model) {
		super(id, model);

		add(new BootstrapFlavor());
	}

}

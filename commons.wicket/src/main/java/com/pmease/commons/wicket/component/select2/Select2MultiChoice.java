package com.pmease.commons.wicket.component.select2;

import java.util.Collection;

import org.apache.wicket.model.IModel;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public class Select2MultiChoice<T> extends com.vaynberg.wicket.select2.Select2MultiChoice<T> {

	public Select2MultiChoice(String id, IModel<Collection<T>> model, ChoiceProvider<T> provider) {
		super(id, model, provider);
		
		add(new BootstrapFlavor());
	}

	public Select2MultiChoice(String id, IModel<Collection<T>> model) {
		super(id, model);
		
		add(new BootstrapFlavor());
	}

	public Select2MultiChoice(String id) {
		super(id);

		add(new BootstrapFlavor());
	}

}

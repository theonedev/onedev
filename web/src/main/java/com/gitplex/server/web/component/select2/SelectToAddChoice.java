package com.gitplex.server.web.component.select2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class SelectToAddChoice<T> extends Select2Choice<T> {

	private transient T selection;
	
	public SelectToAddChoice(String id, ChoiceProvider<T> choiceProvider) {
		super(id);
		
		setModel(new IModel<T>() {

			@Override
			public void detach() {
			}

			@Override
			public T getObject() {
				return selection;
			}

			@Override
			public void setObject(T object) {
				selection = object;
			}
			
		});

		setProvider(choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				if (selection != null) {
					onSelect(target, selection);
					selection = null;
				}
				String script = String.format("$('#%s').select2('data', null);", getMarkupId());
				target.appendJavaScript(script);
			}
					
		});
	}

	protected abstract void onSelect(AjaxRequestTarget target, T selection);
}

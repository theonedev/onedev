package com.pmease.commons.wicket.component.select2;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.model.IModel;

import com.vaynberg.wicket.select2.ChoiceProvider;

@SuppressWarnings("serial")
public abstract class SelectToAddChoice<T> extends Select2Choice<T> {

	private final String placeHolder;
	
	private transient T selection;
	
	public SelectToAddChoice(String id, ChoiceProvider<T> choiceProvider, String placeHolder) {
		super(id);
		
		setModel(new IModel<T>() {

			@Override
			public void detach() {
			}

			@Override
			public T getObject() {
				return null;
			}

			@Override
			public void setObject(T object) {
				selection = object;
			}
			
		});

		setProvider(choiceProvider);
		
		this.placeHolder = placeHolder;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(SelectToAddChoice.this);
				String script = String.format(""
						+ "var input = $('#%s').prev().find('a.select2-choice'); "
						+ "input.addClass('select2-default');"
						+ "input.find('>span').html('%s');", 
						getMarkupId(), placeHolder);
				target.appendJavaScript(script);
				
				if (selection != null)
					onSelect(target, selection);
			}
					
		});
	}

	protected abstract void onSelect(AjaxRequestTarget target, T selection);
}

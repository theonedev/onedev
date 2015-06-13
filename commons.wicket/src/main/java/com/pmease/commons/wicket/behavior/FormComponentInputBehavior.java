package com.pmease.commons.wicket.behavior;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;

@SuppressWarnings("serial")
public abstract class FormComponentInputBehavior extends AjaxFormComponentUpdatingBehavior{

	private String input;
	
	public FormComponentInputBehavior() {
		super("input");
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
		FormComponent<?> component = (FormComponent<?>) getComponent();

		// IE triggers "input" event when the focused on the search input even if nothing is 
		// input into search box yet. To work around this issue, we compare search string 
		// against previous value to only update the branches table if there is an actual 
		// change.
		String newInput = component.getInput();
		if (!ObjectUtils.equals(newInput, input)) {
			input = newInput;
			onInput(target);
		}
	}

	protected abstract void onInput(AjaxRequestTarget target);
}

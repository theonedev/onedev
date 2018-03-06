package io.onedev.server.web.behavior;

import java.util.Objects;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;

@SuppressWarnings("serial")
public abstract class InputChangeBehavior extends AjaxFormComponentUpdatingBehavior{

	private String input;
	
	public InputChangeBehavior() {
		super("inputchange");
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
		FormComponent<?> component = (FormComponent<?>) getComponent();

		// IE triggers "input" event when the focused on the search input even if nothing is 
		// input into search box yet. To work around this issue, we compare search string 
		// against previous value to only update the branches table if there is an actual 
		// change.
		String newInput = component.getInput();
		if (!Objects.equals(newInput, input)) {
			input = newInput;
			onInputChange(target);
		}
	}

	protected abstract void onInputChange(AjaxRequestTarget target);
}

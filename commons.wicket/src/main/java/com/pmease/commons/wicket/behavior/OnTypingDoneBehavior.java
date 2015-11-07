package com.pmease.commons.wicket.behavior;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponent;

import com.pmease.commons.wicket.assets.doneevents.DoneEventsResourceReference;

@SuppressWarnings("serial")
public abstract class OnTypingDoneBehavior extends AjaxFormComponentUpdatingBehavior{

	private int timeout;
	
	private String input;
	
	public OnTypingDoneBehavior(int timeout) {
		super("donetyping");
		this.timeout = timeout;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(DoneEventsResourceReference.INSTANCE));
		String script = String.format(""
				+ "$('#%s').doneEvents('input', function() {"
				+ "  $(this).trigger('donetyping');"
				+ "}, %s);",
				component.getMarkupId(true), timeout);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
		// IE triggers "input" event when the focused on the search input even if nothing is 
		// input into search box yet. To work around this issue, we compare search string 
		// against previous value to only update the branches table if there is an actual 
		// change.
		String newInput = ((FormComponent<?>)getComponent()).getInput();
		if (!ObjectUtils.equals(newInput, input)) {
			input = newInput;
			onTypingDone(target);
		}
	}

	protected abstract void onTypingDone(AjaxRequestTarget target);
}

package io.onedev.server.web.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import io.onedev.server.web.asset.doneevents.DoneEventsResourceReference;

public abstract class OnTypingDoneBehavior extends AjaxFormComponentUpdatingBehavior {

	private int timeout;
	
	public OnTypingDoneBehavior(int timeout) {
		super("donetyping");
		this.timeout = timeout;
	}

	public OnTypingDoneBehavior() {
		this(250);
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		response.render(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		String script = String.format(""
				+ "var $input = $('#%s');"
				+ "$input.doneEvents('input', function() {"
				+ "  $(this).trigger('donetyping');"
				+ "}, %s);",
				component.getMarkupId(true), timeout);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	protected void onUpdate(AjaxRequestTarget target) {
		onTypingDone(target);
	}

	protected abstract void onTypingDone(AjaxRequestTarget target);
}

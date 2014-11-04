package com.pmease.commons.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

@SuppressWarnings("serial")
public abstract class OnTypingDoneBehavior extends AjaxFormComponentUpdatingBehavior{

	private int timeout;
	
	public OnTypingDoneBehavior(int timeout) {
		super("donetyping");
		this.timeout = timeout;
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);

		String script = String.format("pmease.commons.setupDoneTyping('%s', %d);",
				component.getMarkupId(true), timeout);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

}

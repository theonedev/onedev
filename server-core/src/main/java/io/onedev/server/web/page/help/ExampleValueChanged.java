package io.onedev.server.web.page.help;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class ExampleValueChanged extends AjaxPayload {

	public ExampleValueChanged(AjaxRequestTarget target) {
		super(target);
	}

}
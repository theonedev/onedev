package com.pmease.gitop.web.common.wicket.component.vex;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.jackson.JsOptions;

public class Vex {

	private final JsOptions options = new JsOptions();
	
	public void open(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("vex.open(%s)", options.toString()));
	}
	
	public JsOptions getOptions() {
		return options;
	}
}

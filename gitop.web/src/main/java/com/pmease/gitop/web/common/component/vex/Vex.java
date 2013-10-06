package com.pmease.gitop.web.common.component.vex;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.util.Options;

public class Vex {

	private final Options options = new Options();
	
	public void open(AjaxRequestTarget target) {
		target.appendJavaScript(String.format("vex.open(%s)", options.toString()));
	}
	
	public Options getOptions() {
		return options;
	}
}

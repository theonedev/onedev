package com.pmease.gitop.web.common.wicket.component.messenger;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.commons.jackson.JsOptions;

public class Messenger {

	static enum Type {
		INFO, SUCCESS, WARNING, ERROR
	}

	private final String jsFunc;
	
	Messenger(String jsFunc) {
		this.jsFunc = jsFunc;
	}
	
	public void run(AjaxRequestTarget target) {
		target.appendJavaScript(jsFunc);
	}
	
	public static Messenger info(String message) {
		return message(message, Type.INFO);
	}

	public static Messenger success(String message) {
		return message(message, Type.SUCCESS);
	}

	public static Messenger error(String message) {
		return message(message, Type.ERROR);
	}
	
	public static Messenger warn(String message) {
		return message(message, Type.WARNING);
	}

	private static Messenger message(String message, Type type) {
		return post(new JsOptions()
						.put("message", message)
						.put("type", type.name().toLowerCase())
						.put("showCloseButton", true));
	}

	public static Messenger post(JsOptions options) {
		return new Messenger(String.format("Messenger().post(%s);",
				options.toString()));
	}

	public static Messenger run(JsOptions options) {
		return new Messenger(String.format("Messenger().run(%s);",
				options.toString()));
	}

	public static Messenger update(JsOptions options) {
		return new Messenger(String.format("Messenger().update(%s);",
				options.toString()));
	}
}

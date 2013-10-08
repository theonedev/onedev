package com.pmease.gitop.web.common.component.messenger;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.pmease.gitop.web.common.util.Options;

public class Messenger {

	static enum Type {
		INFO, SUCCESS, ERROR
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

	private static Messenger message(String message, Type type) {
		return post(new Options()
						.set("message", message)
						.set("type", type.name().toLowerCase())
						.set("showCloseButton", true));
	}

	public static Messenger post(Options options) {
		return new Messenger(String.format("Messenger().post(%s);",
				options.toString()));
	}

	public static Messenger run(Options options) {
		return new Messenger(String.format("Messenger().run(%s);",
				options.toString()));
	}

	public static Messenger update(Options options) {
		return new Messenger(String.format("Messenger().update(%s);",
				options.toString()));
	}
}

package io.onedev.server.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Translation {

	private final static ResourceBundle translation;
	

	static {
		ResourceBundle bundle;
		try {
			// development location
			bundle = ResourceBundle.getBundle("io/onedev/server/web/WebApplication", new Locale("zh", "CN"));
		} catch (MissingResourceException e) {
			// runtime location
			bundle = ResourceBundle.getBundle("WebApplication", new Locale("zh", "CN"));
		}
		translation = bundle;
	}

	public static String get(String key) {
		if (translation.containsKey(key)) {
			return translation.getString(key).trim();
		}
		return key;
	}
	
}
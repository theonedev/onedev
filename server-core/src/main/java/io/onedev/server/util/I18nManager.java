package io.onedev.server.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18nManager {

	private static ResourceBundle bundle;

	static {
		// Load the bundle using the custom control
		bundle = ResourceBundle.getBundle("messages", new Locale("zh", "CN"));
	}

	public static void setLocale(String languageCode) {
		bundle = ResourceBundle.getBundle("messages", new Locale(languageCode));
	}

	public static String getString(String key) {
		if (bundle.containsKey(key)) {
			return bundle.getString(key);
		} else {
			return key;
		}
	}
}


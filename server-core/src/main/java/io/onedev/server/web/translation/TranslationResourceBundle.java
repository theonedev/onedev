package io.onedev.server.web.translation;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

public abstract class TranslationResourceBundle extends ResourceBundle {

	@Override
	protected Object handleGetObject(String key) {
		return getContents().get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(getContents().keySet());
	}

	protected abstract Map<String, String> getContents();

}

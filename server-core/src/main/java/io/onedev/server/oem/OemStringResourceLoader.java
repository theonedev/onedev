package io.onedev.server.oem;

import io.onedev.server.web.translation.TranslationStringResourceLoader;
import org.apache.wicket.Component;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

import static io.onedev.server.oem.OemUtils.interpolate;
import static io.onedev.server.oem.OemUtils.interpolateHtml;

public class OemStringResourceLoader extends TranslationStringResourceLoader {

	@Override
	public String loadStringResource(Class<?> clazz, String key, Locale locale, String style, String variation) {
		return loadStringResource(key, it -> super.loadStringResource(clazz, it, locale, style, variation));
	}

	@Override
	public String loadStringResource(Component component, String key, Locale locale, String style, String variation) {
		return loadStringResource(key, it -> super.loadStringResource(component, it, locale, style, variation));
	}

	@Nullable
	private String loadStringResource(String key, Function<String, String> loader) {
		var html = key.startsWith("th:");
		if (html)
			key = "t:" + key.substring(3);
		var translated = loader.apply(key);
		if (translated == null)
			return null;
		return html ? interpolateHtml(translated) : interpolate(translated);
	}

}

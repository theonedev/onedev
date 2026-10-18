package io.onedev.server.oem;

import io.onedev.server.web.translation.TranslationStringResourceLoader;
import org.apache.wicket.Component;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

import static io.onedev.server.oem.OemUtils.interpolate;
import static io.onedev.server.oem.OemUtils.useDefaultUrls;
import static io.onedev.server.oem.OemUtils.useUrlPlaceholders;

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
		var translated = loader.apply(key);
		if (translated == null)
			return null;
		// Existing generated translations still use the default URLs in their keys and values.
		if (key.equals("t: " + translated) || key.equals("t:" + translated)) {
			var defaultKey = useDefaultUrls(key);
			if (!defaultKey.equals(key))
				translated = loader.apply(defaultKey);
		}
		return interpolate(useUrlPlaceholders(translated));
	}

}

package io.onedev.server.web.translation;

import java.util.HashMap;
import java.util.Map;

public class Translation_de extends TranslationResourceBundle {

	private static final Map<String, String> m = new HashMap<>();

	static {
		init();
		Translation.watchUpdate(Translation_de.class, () -> {
			init();
		});
	}

	private static void init() {
		m.clear();
	}
		
	@Override
	protected Map<String, String> getContents() {
		return m;		
	}
	
}

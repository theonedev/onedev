package io.onedev.server.web.translation;

import org.apache.wicket.Localizer;

public class Translation extends TranslationResourceBundle {

	public static String _T(String key) {
		return Localizer.get().getString("t: " + key, null);
	}

	@Override
	protected Object[][] getAutoContents() {
		return new Object[][] {
        };
	}

	@Override
	protected Object[][] getManualContents() {
		return new Object[][] {
        };
	}

}

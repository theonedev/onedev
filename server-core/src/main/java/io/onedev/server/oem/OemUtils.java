package io.onedev.server.oem;

import io.onedev.server.OneDev;

import static io.onedev.server.oem.OemService.DEFAULT_DOCSITE;
import static io.onedev.server.oem.OemService.DEFAULT_WEBSITE;

public class OemUtils {

	public static String interpolate(String text) {
		if (text.contains("${docsite}") || text.contains("${website}")) {
			var oemService = OneDev.getInstance(OemService.class);
			return text.replace("${docsite}", oemService.getDocsite())
					.replace("${website}", oemService.getWebsite());
		}
		return text;
	}

	public static String useDefaultUrls(String text) {
		return text.replace("${docsite}", DEFAULT_DOCSITE).replace("${website}", DEFAULT_WEBSITE);
	}

	public static String useUrlPlaceholders(String text) {
		return text.replace(DEFAULT_DOCSITE, "${docsite}").replace(DEFAULT_WEBSITE, "${website}");
	}

}

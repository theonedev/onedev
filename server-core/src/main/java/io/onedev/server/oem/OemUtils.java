package io.onedev.server.oem;

import io.onedev.server.OneDev;
import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.unbescape.html.HtmlEscape;

import java.util.regex.Pattern;

public class OemUtils {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{(productName|docsite|website|contactEmail)}");

	public static String getProductName() {
		return OneDev.getInstance(OemService.class).getProductName();
	}

	public static String interpolate(String text) {
		return interpolate(text, false);
	}

	public static String interpolateHtml(String text) {
		return interpolate(text, true);
	}

	public static String translateHtml(String text) {
		if (Application.exists())
			return Localizer.get().getString("th: " + text, null);
		else
			return interpolateHtml(text);
	}

	private static String interpolate(String text, boolean html) {
		var matcher = PLACEHOLDER.matcher(text);
		if (matcher.find()) {
			var oemService = OneDev.getInstance(OemService.class);
			return matcher.replaceAll(it -> {
				var value = switch (it.group(1)) {
					case "productName" -> oemService.getProductName();
					case "docsite" -> oemService.getDocsite();
					case "contactEmail" -> oemService.getContactEmail();
					default -> oemService.getWebsite();
				};
				return java.util.regex.Matcher.quoteReplacement(html ? HtmlEscape.escapeHtml5(value) : value);
			});
		}
		return text;
	}

}

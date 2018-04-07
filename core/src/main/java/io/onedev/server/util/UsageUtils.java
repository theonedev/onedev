package io.onedev.server.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationMessage;
import io.onedev.utils.StringUtils;

public class UsageUtils {

	public static String getMessage(String objectName, List<String> usages) {
		return objectName + " is still being used by:\n" + StringUtils.join(usages, "\n");
	}
	
	@Nullable
	public static NotificationMessage getNotificationMessage(String objectName, List<String> usages) {
		if (!usages.isEmpty()) {
			String message = getMessage(objectName, usages);
			message = HtmlEscape.escapeHtml5(message);
			message = StringUtils.replace(message, "\n", "<br>");
			return new NotificationMessage(Model.of(message)).escapeModelStrings(false);
		} else {
			return null;
		}
	}
	
	public static List<String> prependCategory(String category, List<String> usages) {
		List<String> prependedUsages = new ArrayList<>();
		for (String usage: usages)
			prependedUsages.add(category + " / " + usage);
		return prependedUsages;
	}
	
}

package io.onedev.server.exception;

import java.util.List;

import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationMessage;
import io.onedev.server.util.UsageUtils;
import io.onedev.utils.StringUtils;

public class InUseException extends OneException {

	private static final long serialVersionUID = 1L;

	private final String objectName;
	
	private final List<String> usages;
	
	public InUseException(String objectName, List<String> usages) {
		super(UsageUtils.getMessage(objectName, usages));
	
		this.objectName = objectName;
		this.usages = usages;
	}

	public String getObjectName() {
		return objectName;
	}

	public List<String> getUsages() {
		return usages;
	}

	public NotificationMessage getNotificationMessage() {
		String message = HtmlEscape.escapeHtml5(getMessage());
		message = StringUtils.replace(message, "\n", "<br>");
		return new NotificationMessage(Model.of(message)).escapeModelStrings(false);
	}
}

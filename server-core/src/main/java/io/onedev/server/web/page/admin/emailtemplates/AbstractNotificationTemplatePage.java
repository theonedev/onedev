package io.onedev.server.web.page.admin.emailtemplates;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.util.CollectionUtils;

public abstract class AbstractNotificationTemplatePage extends AbstractTemplatePage {
	
	public AbstractNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getDefaultTemplate() {
		return EmailTemplates.DEFAULT_NOTIFICATION;
	}

	@Override
	protected String getTemplateHelp(String helpText, Map<String, String> variableHelp) {
		var currentVaribaleHelp = CollectionUtils.newLinkedHashMap(
				"event", _T("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification"),
				"eventSummary", _T("a string representing summary of the event"),
				"eventBody", _T("a string representing body of the event. May be <code>null</code>"),
				"eventUrl", _T("a string representing event detail url"),
				"replyable", _T("a boolean indiciating whether or not topic comment can be created directly by replying the email"),
				"unsubscribable", _T("an <a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/notification/Unsubscribable.java' target='_blank'>object</a> holding unsubscribe information. "
						+ " A <code>null</code> value means that the notification can not be unsubscribed")
		);
		currentVaribaleHelp.putAll(variableHelp);
		
		return super.getTemplateHelp(helpText, currentVaribaleHelp);
	}
	
}
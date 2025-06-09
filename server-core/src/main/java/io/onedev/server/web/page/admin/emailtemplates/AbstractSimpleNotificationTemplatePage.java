package io.onedev.server.web.page.admin.emailtemplates;

import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.util.CollectionUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Map;

public abstract class AbstractSimpleNotificationTemplatePage extends AbstractTemplatePage {
	
	public AbstractSimpleNotificationTemplatePage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getDefaultTemplate() {
		return EmailTemplates.DEFAULT_SIMPLE_NOTIFICATION;
	}

	@Override
	protected String getTemplateHelp(String helpText, Map<String, String> variableHelp) {
		var currentVaribaleHelp = CollectionUtils.newLinkedHashMap(
				"event", _T("<a href='https://code.onedev.io/onedev/server/~files/main/server-core/src/main/java/io/onedev/server/event/Event.java' target='_blank'>event object</a> triggering the notification"),
				"eventSummary", _T("a string representing summary of the event"),
				"eventBody", _T("a string representing body of the event. May be <code>null</code>"),
				"eventUrl", _T("a string representing event detail url")
		);
		currentVaribaleHelp.putAll(variableHelp);
		
		return super.getTemplateHelp(helpText, currentVaribaleHelp);
	}
	
}
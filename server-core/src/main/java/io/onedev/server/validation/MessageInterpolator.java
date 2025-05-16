package io.onedev.server.validation;

import java.util.Locale;

import org.apache.wicket.Session;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import io.onedev.server.web.WebSession;

public class MessageInterpolator extends ParameterMessageInterpolator {
	
	@Override
	public String interpolate(String messageTemplate, Context context) {
		Locale locale = null;
		if (Session.exists()) {
			WebSession session = (WebSession) Session.get();
			locale = session.getLocale();
		}
		if (locale == null)
			locale = Locale.getDefault();
		return interpolate(messageTemplate, context, locale);
	}
	
	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		return super.interpolate(messageTemplate, context, locale);
	}
	
} 
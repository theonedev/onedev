package io.onedev.server.web.page;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.layout.MainMenuCustomization;

public class HomePage extends LayoutPage {

	public static final String PARAM_FAILSAFE = "failsafe";
	
	private final boolean failsafe;
	
	public HomePage(PageParameters params) {
		super(params);
		
		failsafe = params.get(PARAM_FAILSAFE).toBoolean(false);
		
		PageProvider pageProvider = OneDev.getInstance(MainMenuCustomization.class).getHomePage(failsafe);
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Home");
	}

}

package io.onedev.server.web.page.simple.security;

import org.apache.wicket.Session;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.javascript.JavaScriptEscape;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class OAuthCallbackPage extends SimplePage {

	public static final String MOUNT_PATH = "~oauth/callback";
	
	private static final String PARAM_CODE = "code";
	
	private static final String PARAM_STATE = "state";

	private String code;
	
	private String state;
	
	public OAuthCallbackPage(PageParameters params) {
		super(params);
		state = params.get(PARAM_STATE).toString();
		code = params.get(PARAM_CODE).toString();
		
		if (!state.equals(Session.get().getAttribute("oauthState"))) {
			throw new ExplicitException("Invalid state. Please make sure you are visiting "
					+ "OneDev using server url specified in system setting");
		} else {
			Session.get().setAttribute("oauthCode", code);
		}
	}

	@Override
	protected String getTitle() {
		return "Please wait...";
	}

	@Override
	protected String getSubTitle() {
		return null;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (code != null) {
			String script = String.format("window.open('', '%s').callback();", 
					JavaScriptEscape.escapeJavaScript(state));
			response.render(OnDomReadyHeaderItem.forScript(script));
		} else {
			response.render(OnDomReadyHeaderItem.forScript("window.close();"));
		}
	}
	
}

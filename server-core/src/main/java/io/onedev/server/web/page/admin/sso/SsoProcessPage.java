package io.onedev.server.web.page.admin.sso;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.page.DashboardPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.simple.security.LoginPage;

@SuppressWarnings("serial")
public class SsoProcessPage extends BasePage {

	public static final String MOUNT_PATH = "sso";
	
	public static final String STAGE_INITIATE = "initiate";
	
	public static final String STAGE_CALLBACK = "callback";
	
	private static final String PARAM_CONNECTOR = "connector";
	
	private static final String PARAM_STAGE = "stage";
	
	private static final String SESSION_ATTR_REDIRECT_URL = "redirectUrl";
	
	private final SsoConnector connector;
	
	private final String stage;
	
	public SsoProcessPage(PageParameters params) {
		super(params);
		
		stage = params.get(PARAM_STAGE).toString();
		
		try {
			String connectorName = params.get(PARAM_CONNECTOR).toString();
			connector = OneDev.getInstance(SettingManager.class).getSsoConnectors().stream()
					.filter(it->it.getName().equals(connectorName))
					.findFirst()
					.orElse(null);
			
			if (connector == null) 
				throw new AuthenticationException("Unable to find SSO connector: " + connectorName);
			
			if (stage.equals(STAGE_INITIATE)) {
				String redirectUrlAfterLogin;
				Url url = RestartResponseAtInterceptPageException.getOriginalUrl();
				if (url != null) 
					redirectUrlAfterLogin = url.toString();
				else 
					redirectUrlAfterLogin = RequestCycle.get().urlFor(DashboardPage.class, new PageParameters()).toString(); 
				Session.get().bind();
				Session.get().setAttribute(SESSION_ATTR_REDIRECT_URL, redirectUrlAfterLogin);
				connector.initiateLogin();
			} else {
				SsoAuthenticated authenticated = connector.processLoginResponse();
				
				String redirectUrlAfterLogin = (String) Session.get().getAttribute(SESSION_ATTR_REDIRECT_URL);
				if (redirectUrlAfterLogin == null)
					throw new AuthenticationException("unsolicited OIDC authentication response");
				
				WebSession.get().login(authenticated);
				
				throw new RedirectToUrlException(redirectUrlAfterLogin);
			}
		} catch (AuthenticationException e) {
			throw new RestartResponseException(new LoginPage(e.getMessage()));
		}
	}
	
}

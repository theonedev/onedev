package io.onedev.server.mail;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.OAuthUtils;
import org.unbescape.html.HtmlEscape;

public class OAuthAccessToken implements MailCredential {

	private static final long serialVersionUID = 1L;

	private final String tokenEndpoint;
	
	private final String clientId;
	
	private final String clientSecret;
	
	private final String refreshToken;
	
	public OAuthAccessToken(String tokenEndpoint, String clientId, String clientSecret, String refreshToken) {
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.refreshToken = refreshToken;
	}

	@Override
	public String getValue() {
		try {
			return OAuthUtils.getAccessToken(tokenEndpoint, clientId, clientSecret, refreshToken);
		} catch (ExplicitException e) {
			Long userId = SecurityUtils.getUserId();
			if (userId.equals(0L) || userId.equals(User.SYSTEM_ID))
				OneDev.getInstance(AlertManager.class).alert("Failed to get access token of mail server",
						HtmlEscape.escapeHtml5(e.getMessage()), true);
			throw e;
		}
	}

}

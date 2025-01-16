package io.onedev.server.mail;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.AlertManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.oauth.OAuthTokenManager;
import io.onedev.server.util.oauth.RefreshTokenAccessor;
import org.unbescape.html.HtmlEscape;

public class OAuthAccessToken implements MailCredential {

	private static final long serialVersionUID = 1L;

	private final String tokenEndpoint;
	
	private final String clientId;
	
	private final String clientSecret;
	
	private final RefreshTokenAccessor refreshTokenAccessor;
	
	public OAuthAccessToken(String tokenEndpoint, String clientId, String clientSecret,
							RefreshTokenAccessor refreshTokenAccessor) {
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.refreshTokenAccessor = refreshTokenAccessor;
	}

	@Override
	public String getValue() {
		try {
			return OneDev.getInstance(OAuthTokenManager.class).getAccessToken(tokenEndpoint, clientId, clientSecret, refreshTokenAccessor);
		} catch (ExplicitException e) {
			if (SecurityUtils.isAnonymous() || SecurityUtils.isSystem())
				OneDev.getInstance(AlertManager.class).alert("Failed to get access token of mail server",
						HtmlEscape.escapeHtml5(e.getMessage()), true);
			throw e;
		}
	}

}

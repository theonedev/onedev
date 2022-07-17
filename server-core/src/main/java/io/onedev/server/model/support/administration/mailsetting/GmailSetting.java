package io.onedev.server.model.support.administration.mailsetting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.mail.MailCheckSetting;
import io.onedev.server.mail.MailCredential;
import io.onedev.server.mail.MailSendSetting;
import io.onedev.server.mail.OAuthAccessToken;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.editable.annotation.RefreshToken;

@Editable(order=200, name="Gmail")
public class GmailSetting extends MailSetting {

	private static final long serialVersionUID = 1L;
	
	private static final String AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
	
	private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
	
	private String clientId;
	
	private String clientSecret;
	
	private String accountName;
	
	private String refreshToken;
	
	private InboxPollSetting inboxPollSetting;
	
	@Editable(order=100, description="Client ID of this OneDev instance registered in Google cloud")
	@NotEmpty
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Editable(order=200, description="Client secret of this OneDev instance registered in Google cloud")
	@Password
	@NotEmpty
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Editable(order=300, description="Specify account name to login to Gmail to send/receive emails. "
			+ "This should be a Gmail address")
	@Email
	@NotEmpty
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@Editable(order=400, description="Long-live refresh token of above account which will be used to generate "
			+ "access token to access Gmail. <b class='text-info'>TIPS: </b> you may use the button at right side "
			+ "of this field to generate refresh token. Note that whenever client id, client secret, or account "
			+ "name is changed, refresh token should be re-generated")
	@RefreshToken("getRefreshTokenCallback")
	@NotEmpty
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	@SuppressWarnings("unused")
	private static RefreshToken.Callback getRefreshTokenCallback() {
		String clientId = (String) EditContext.get().getInputValue("clientId");
		if (clientId == null)
			throw new ExplicitException("Client ID needs to be specified to generate refresh token");
		String clientSecret = (String) EditContext.get().getInputValue("clientSecret");
		if (clientSecret == null)
			throw new ExplicitException("Client secret needs to be specified to generate refresh token");
		
		String accountName = (String) EditContext.get().getInputValue("accountName");
		if (accountName == null)
			throw new ExplicitException("Account name needs to be specified to generate refresh token");

		Collection<String> scopes = Lists.newArrayList("https://mail.google.com/");
		
		return new RefreshToken.Callback() {

			@Override
			public String getAuthorizeEndpoint() {
				return AUTHORIZE_ENDPOINT;
			}

			@Override
			public Map<String, String> getAuthorizeParams() {
				Map<String, String> params = new HashMap<>();
				params.put("access_type", "offline");
				params.put("login_hint", accountName);
				params.put("prompt", "consent");
				return params;
			}
			
			@Override
			public String getClientId() {
				return clientId;
			}

			@Override
			public String getClientSecret() {
				return clientSecret;
			}

			@Override
			public String getTokenEndpoint() {
				return TOKEN_ENDPOINT;
			}

			@Override
			public Collection<String> getScopes() {
				return scopes;
			}

		};
	}
	
	@Editable(order=500, name="Check Incoming Email")
	public InboxPollSetting getInboxPollSetting() {
		return inboxPollSetting;
	}

	public void setInboxPollSetting(InboxPollSetting inboxPollSetting) {
		this.inboxPollSetting = inboxPollSetting;
	}

	@Override
	public MailSendSetting getSendSetting() {
		MailCredential smtpCredential = new OAuthAccessToken(
				TOKEN_ENDPOINT, clientId, clientSecret, refreshToken);
		return new MailSendSetting("smtp.gmail.com", 587, accountName, smtpCredential, 
				accountName, true, getTimeout());
	}

	@Override
	public MailCheckSetting getCheckSetting() {
		if (inboxPollSetting != null) {
			MailCredential imapCredential = new OAuthAccessToken(
					TOKEN_ENDPOINT, clientId, clientSecret, refreshToken);
			
			return new MailCheckSetting("imap.gmail.com", 993, 
					accountName, imapCredential, accountName, true, 
					inboxPollSetting.getPollInterval(), getTimeout());
		} else {
			return null;
		}
	}

}

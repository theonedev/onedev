package io.onedev.server.model.support.administration.mailsetting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

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

@Editable(order=100, name="Office 365", description=""
		+ "Office 365 mail provider with OAuth authentication. "
		+ "Click <a href='https://robinshen.medium.com/oauth-authentication-for-office-365-mail-service-ff9497caf074' target='_blank'>here</a> for an example setup")
public class Office365Setting extends MailSetting {

	private static final long serialVersionUID = 1L;
	
	private String clientId;
	
	private String tenantId;
	
	private String clientSecret;
	
	private String userPrincipalName;
	
	private String refreshToken;
	
	private String emailAddress;
	
	private InboxPollSetting inboxPollSetting;
	
	@Editable(order=100, name="Application (client) ID", description="Specify application (tenant) ID of this OneDev instance registered in Azure AD")
	@NotEmpty
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Editable(order=150, name="Directory (tenant) ID", description="Specify ID of the Azure AD this OneDev instance is registered in")
	@NotEmpty
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Editable(order=200, description="Client secret of this OneDev instance registered in Azure AD")
	@Password
	@NotEmpty
	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	@Editable(order=300, description="Principal name of the account to login into office 365 mail server to "
			+ "send/receive emails. It is normally primary email address of the account")
	@NotEmpty
	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	@Editable(order=400, description="Long-live refresh token of above account which will be used to generate access token "
			+ "to access office 365 mail server. <b class='text-info'>TIPS: </b> you may use the button at right "
			+ "side of this field to login to your office 365 account and generate refresh token. Note that whenever "
			+ "tenant id, client id, client secret, or user principal name is changed, refresh token should be re-generated")
	@RefreshToken("getRefreshTokenCallback")
	@NotEmpty
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	@Editable(order=410, name="System Email Address", description="Primary or alias email address of above account "
			+ "to be used as sender address of various notifications. Emails targeting this address will also be "
			+ "checked to post various comments if <code>Check Incoming Email</code> option is enabled below")
	@Email
	@NotEmpty
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	private static String getTokenEndpoint(String tenantId) {
		return String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);
	}

	@Editable(order=450, name="Check Incoming Email", description="Enable this to post issue and pull request comments via email. "
			+ "<b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> "
			+ "needs to be enabled for above email address, as OneDev uses it to track issue and pull request contexts")
	public InboxPollSetting getInboxPollSetting() {
		return inboxPollSetting;
	}

	public void setInboxPollSetting(InboxPollSetting inboxPollSetting) {
		this.inboxPollSetting = inboxPollSetting;
	}

	@SuppressWarnings("unused")
	private static RefreshToken.Callback getRefreshTokenCallback() {
		String tenantId = (String) EditContext.get().getInputValue("tenantId");
		if (tenantId == null)
			throw new ExplicitException("Directory (tenant) ID needs to be specified to generate refresh token");
		String clientId = (String) EditContext.get().getInputValue("clientId");
		if (clientId == null)
			throw new ExplicitException("Application (client) ID needs to be specified to generate refresh token");
		String clientSecret = (String) EditContext.get().getInputValue("clientSecret");
		if (clientSecret == null)
			throw new ExplicitException("Client secret needs to be specified to generate refresh token");
		
		String userPrincipalName = (String) EditContext.get().getInputValue("userPrincipalName");
		if (userPrincipalName == null)
			throw new ExplicitException("User principal name needs to be specified to generate refresh token");

		Collection<String> scopes = Lists.newArrayList(
				"https://outlook.office.com/SMTP.Send", 
				"https://outlook.office.com/IMAP.AccessAsUser.All", 
				"offline_access");
		
		String authorizeEndpoint = String.format(
				"https://login.microsoftonline.com/%s/oauth2/v2.0/authorize", tenantId);
		String tokenEndpoint = getTokenEndpoint(tenantId);
		
		return new RefreshToken.Callback() {

			@Override
			public String getAuthorizeEndpoint() {
				return authorizeEndpoint;
			}

			@Override
			public Map<String, String> getAuthorizeParams() {
				Map<String, String> params = new HashMap<>();
				params.put("login_hint", userPrincipalName);
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
				return tokenEndpoint;
			}

			@Override
			public Collection<String> getScopes() {
				return scopes;
			}

		};
	}
	
	@Override
	public MailSendSetting getSendSetting() {
		MailCredential smtpCredential = new OAuthAccessToken(
				getTokenEndpoint(tenantId), clientId, clientSecret, refreshToken);
		return new MailSendSetting("smtp.office365.com", 587, userPrincipalName, smtpCredential, 
				emailAddress, true, getTimeout());
	}

	@Override
	public MailCheckSetting getCheckSetting() {
		if (inboxPollSetting != null) {
			String imapUser = getUserPrincipalName();
			MailCredential imapCredential = new OAuthAccessToken(
					getTokenEndpoint(tenantId), clientId, clientSecret, refreshToken);
			
			return new MailCheckSetting("outlook.office365.com", 993, 
					imapUser, imapCredential, emailAddress, true, 
					inboxPollSetting.getPollInterval(), getTimeout());
		} else {
			return null;
		}
	}

}

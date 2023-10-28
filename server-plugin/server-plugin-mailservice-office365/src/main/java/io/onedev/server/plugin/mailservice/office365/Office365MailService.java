package io.onedev.server.plugin.mailservice.office365;

import com.google.common.collect.Lists;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;
import io.onedev.server.annotation.RefreshToken;
import io.onedev.server.mail.*;
import io.onedev.server.model.support.administration.mailservice.ImapImplicitSsl;
import io.onedev.server.model.support.administration.mailservice.MailService;
import io.onedev.server.model.support.administration.mailservice.SmtpExplicitSsl;
import io.onedev.server.util.EditContext;
import org.jetbrains.annotations.Nullable;

import javax.mail.Message;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Editable(name="Microsoft 365", order=200)
public class Office365MailService implements MailService {

	private static final long serialVersionUID = 1L;
	
	private String clientId;

	private String tenantId;

	private String clientSecret;

	private String userPrincipalName;

	private String refreshToken;
	
	private String systemAddress;
	
	private InboxPollSetting inboxPollSetting;
	
	private int timeout = 60;
	
	private transient MailPosition mailPosition;
	
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

	@Editable(order=300, description="Principal name of the account to login into office 365 mail server to " +
			"send/receive emails. Make sure this account <b>owns</b> the registered application indicated by " +
			"application id above")
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

	@Editable(order=410, name="System Email Address", description="Primary or alias email address of above account " +
			"to be used as sender address of various email notifications. User can also reply to this address to post " +
			"issue or pull request comments via email if <code>Check Incoming Email</code> option is enabled below")
	@Email
	@NotEmpty
	public String getSystemAddress() {
		return systemAddress;
	}

	public void setSystemAddress(String systemAddress) {
		this.systemAddress = systemAddress;
	}
	
	@Editable(order=450, name="Check Incoming Email", description="Enable this to process issue or pull request comments posted via email. "
			+ "<b class='text-danger'>NOTE:</b> <a href='https://docs.microsoft.com/en-us/exchange/recipients-in-exchange-online/plus-addressing-in-exchange-online' target='_blank'>Sub addressing</a> "
			+ "needs to be enabled for system email address above, as OneDev uses it to track issue and pull request contexts")
	public InboxPollSetting getInboxPollSetting() {
		return inboxPollSetting;
	}

	public void setInboxPollSetting(InboxPollSetting inboxPollSetting) {
		this.inboxPollSetting = inboxPollSetting;
	}
	
	@Editable(order=10000, description="Specify timeout in seconds when communicating with mail server")
	@Min(value=5, message="This value should not be less than 5")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private static String getTokenEndpoint(String tenantId) {
		return String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);
	}

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

	private SmtpSetting getSmtpSetting() {
		MailCredential smtpCredential = new OAuthAccessToken(
				getTokenEndpoint(tenantId), clientId, clientSecret, refreshToken);
		return new SmtpSetting("smtp.office365.com", new SmtpExplicitSsl(), userPrincipalName,
				smtpCredential, getTimeout());
	}	
	
	@Override
	public void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
						 String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
						 @Nullable String senderName, @Nullable String references) {
		getMailManager().sendMail(getSmtpSetting(), toList, ccList, bccList, subject, htmlBody, textBody, 
				replyAddress, senderName, getSystemAddress(), references);
	}

	@Override
	public InboxMonitor getInboxMonitor() {
		var imapUser = getUserPrincipalName();
		var imapCredential = new OAuthAccessToken(
				getTokenEndpoint(tenantId), clientId, clientSecret, refreshToken);
		if (inboxPollSetting != null) {
			var imapSetting = new ImapSetting("outlook.office365.com",
					new ImapImplicitSsl(), imapUser, imapCredential,
					inboxPollSetting.getPollInterval(), getTimeout());
			return new InboxMonitor() {
				@Override
				public Future<?> monitor(Consumer<Message> messageConsumer, boolean testMode) {
					if (mailPosition == null)
						mailPosition = new MailPosition();
					return getMailManager().monitorInbox(imapSetting, getSystemAddress(),
							messageConsumer, mailPosition, testMode);
				}

				@Override
				public boolean isMonitorSystemAddressOnly() {
					return inboxPollSetting.isMonitorSystemAddressOnly();
				}
			};
		} else {
			return null;
		}
	}

	private MailManager getMailManager() {
		return OneDev.getInstance(MailManager.class);
	}
	
}

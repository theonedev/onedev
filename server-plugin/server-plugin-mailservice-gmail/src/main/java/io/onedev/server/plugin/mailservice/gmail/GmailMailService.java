package io.onedev.server.plugin.mailservice.gmail;

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

@Editable(name="Gmail", order=300)
public class GmailMailService implements MailService {

	private static final long serialVersionUID = 1L;

	private static final String AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";

	private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
	
	private String clientId;

	private String clientSecret;

	private String accountName;

	private String refreshToken;
	
	private String systemAddress;
	
	private InboxPollSetting inboxPollSetting;
	
	private int timeout = 60;
	
	private transient MailPosition mailPosition;

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

	@Editable(order=300, description="Specify account name to login to Gmail to send/receive email")
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
	
	@Editable(order=500, name="Check Incoming Email", description="Enable this to process issue or pull request comments posted via email")
	public InboxPollSetting getInboxPollSetting() {
		return inboxPollSetting;
	}

	public void setInboxPollSetting(InboxPollSetting inboxPollSetting) {
		this.inboxPollSetting = inboxPollSetting;
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
	
	private SmtpSetting getSmtpSetting() {
		MailCredential smtpCredential = new OAuthAccessToken(
				TOKEN_ENDPOINT, clientId, clientSecret, refreshToken);
		return new SmtpSetting("smtp.gmail.com", new SmtpExplicitSsl(), accountName, smtpCredential,
				getTimeout());
	}
	
	@Editable(order=10000, description="Specify timeout in seconds when communicating with mail server")
	@Min(value=5, message="This value should not be less than 5")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
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
		if (inboxPollSetting != null) {
			var imapCredential = new OAuthAccessToken(
					TOKEN_ENDPOINT, clientId, clientSecret, refreshToken);
			var imapSetting = new ImapSetting("imap.gmail.com",
					new ImapImplicitSsl(), accountName, imapCredential,
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

package io.onedev.server.ee.sendgrid;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.SubscriptionRequired;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.annotation.Editable;
import io.onedev.server.ee.NoSubscriptionException;
import io.onedev.server.mail.BasicAuthPassword;
import io.onedev.server.mail.InboxMonitor;
import io.onedev.server.mail.MailManager;
import io.onedev.server.mail.SmtpSetting;
import io.onedev.server.model.support.administration.mailservice.MailService;
import io.onedev.server.model.support.administration.mailservice.SmtpImplicitSsl;
import org.jetbrains.annotations.Nullable;

import javax.mail.Message;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Editable(name="SendGrid", order=400, descriptionProvider = "getClassDescription")
@SubscriptionRequired
public class SendgridMailService implements MailService {

	private static final long serialVersionUID = 1L;
	
	private String apiKey;
	
	private String systemAddress;
	
	private int timeout = 60;

	private SendgridWebhookSetting webhookSetting;
	
	@Editable(order=100)
	@NotEmpty
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@Editable(order=410, name="System Email Address", description="This address should be <code>verified sender</code> " +
			"in SendGrid and will be used as sender address of various email notifications. One can also reply to this " +
			"address to post issue or pull request comments if <code>Receive Posted Email</code> option is enabled below")
	@Email
	@NotEmpty
	@Override
	public String getSystemAddress() {
		return systemAddress;
	}

	public void setSystemAddress(String systemAddress) {
		this.systemAddress = systemAddress;
	}

	@Editable(order=500, description="Specify timeout in seconds when communicating with mail server")
	@Min(value=5, message="This value should not be less than 5")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Editable(order=600, name="Receive Posted Email", description = "Enable this to process issue or pull request comments posted via email")
	public SendgridWebhookSetting getWebhookSetting() {
		return webhookSetting;
	}

	public void setWebhookSetting(SendgridWebhookSetting sendgridSetting) {
		this.webhookSetting = sendgridSetting;
	}

	@Override
	public void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
						 String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
						 @Nullable String senderName, @Nullable String references) {
		if (isSubscriptionActive()) {
			var smtpSetting = new SmtpSetting("smtp.sendgrid.net", new SmtpImplicitSsl(),
					"apikey", new BasicAuthPassword(getApiKey()), getTimeout());
			getMailManager().sendMail(smtpSetting, toList, ccList, bccList, subject,
					htmlBody, textBody, replyAddress, senderName, getSystemAddress(), references);
		} else {
			throw new NoSubscriptionException();
		}
	}

	@Override
	public InboxMonitor getInboxMonitor() {
		if (webhookSetting != null) {
			return new InboxMonitor() {
				@Override
				public Future<?> monitor(Consumer<Message> messageConsumer, boolean testMode) {
					return OneDev.getInstance(ExecutorService.class).submit(() -> {
						if (isSubscriptionActive()) {
							var messageManager = OneDev.getInstance(MessageManager.class);
							var target = new MessageTarget(webhookSetting.getSecret());
							messageManager.register(target);
							try {
								while (true)
									messageConsumer.accept(target.getQueue().take());
							} finally {
								messageManager.unregister(target);
							}
						} else {
							throw new NoSubscriptionException();
						}
					});
				}

				@Override
				public boolean isMonitorSystemAddressOnly() {
					return webhookSetting.isMonitorSystemAddressOnly();
				}
			};
		} else {
			return null;
		}
	}
	
	private static boolean isSubscriptionActive() {
		return OneDev.getInstance(SubscriptionManager.class).isSubscriptionActive();
	}
	
	private MailManager getMailManager() {
		return OneDev.getInstance(MailManager.class);
	}
	
	private static String getClassDescription() {
		if (!isSubscriptionActive()) {
			return "<b class='text-danger'>NOTE: </b>SendGrid integration is an enterprise feature. " +
					"<a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days";
		} else {
			return null;
		}
	}
}

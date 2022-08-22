package io.onedev.server.mail;

import java.util.Collection;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

public interface MailManager {
	
	public static final String TEST_SUB_ADDRESS = "test~subaddressing";
	
	public static final String INCOMING_CONTENT_MARKER = "no-color";
	
	void sendMail(Collection<String> toList, Collection<String> ccList, 
			Collection<String> bccList, String subject, String htmlBody, 
			String textBody, @Nullable String replyAddress, @Nullable String references);
	
	void sendMail(MailSendSetting sendSetting, Collection<String> toList, Collection<String> ccList, 
			Collection<String> bccList, String subject, String htmlBody, String textBody, 
			@Nullable String replyAddress, @Nullable String references);
	
	void sendMailAsync(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
			String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
			@Nullable String references);
	
	@Nullable
	String getReplyAddress(Issue issue);
	
	@Nullable
	String getReplyAddress(PullRequest request);

	@Nullable
	String getUnsubscribeAddress(Issue issue);

	@Nullable
	public String getUnsubscribeAddress(PullRequest request);
	
	Future<?> monitorInbox(MailCheckSetting mailCheckSetting, MessageListener listener, MailPosition MailPosition);

}

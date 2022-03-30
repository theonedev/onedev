package io.onedev.server.notification;

import java.util.Collection;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.ReceiveMailSetting;

public interface MailManager {
	
	public static final String TEST_SUB_ADDRESS = "test~subaddressing";
	
	void sendMail(Collection<String> toList, Collection<String> ccList, 
			Collection<String> bccList, String subject, String htmlBody, 
			String textBody, @Nullable String replyAddress, @Nullable String references);
	
	void sendMail(MailSetting mailSetting, Collection<String> toList, Collection<String> ccList, 
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
	
	Future<?> monitorInbox(ReceiveMailSetting receiveMailSetting, int timeout, 
			MessageListener listener, MailPosition MailPosition);

}

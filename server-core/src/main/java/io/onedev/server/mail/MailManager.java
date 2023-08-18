package io.onedev.server.mail;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.Future;

public interface MailManager {
	
	public static final String TEST_SUB_ADDRESS = "test~subaddressing";
	
	public static final String COMMENT_MARKER = "no-color";
	
	void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
				  String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
				  @Nullable String senderName, @Nullable String references);
	
	void sendMail(MailSendSetting sendSetting, Collection<String> toList, Collection<String> ccList, 
				  Collection<String> bccList, String subject, String htmlBody, String textBody, 
				  @Nullable String replyAddress, @Nullable String senderName, @Nullable String references);
	
	void sendMailAsync(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
					   String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
					   @Nullable String senderName, @Nullable String references);
	
	@Nullable
	String getReplyAddress(Issue issue);
	
	@Nullable
	String getReplyAddress(PullRequest request);

	@Nullable
	String getUnsubscribeAddress(Issue issue);

	@Nullable
	public String getUnsubscribeAddress(PullRequest request);
	
	boolean isMailContent(String comment);
	
	String toPlainText(String mailContent);
	
	Future<?> monitorInbox(MailCheckSetting checkSetting, MessageListener listener, 
						   MailPosition lastPosition, boolean testMode);

}

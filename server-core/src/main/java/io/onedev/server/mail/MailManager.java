package io.onedev.server.mail;

import io.onedev.server.model.Issue;
import io.onedev.server.model.PullRequest;

import javax.annotation.Nullable;
import javax.mail.Message;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface MailManager {
	
	String TEST_SUB_ADDRESS = "test~subaddressing";
	
	String COMMENT_MARKER = "no-color";
	
	void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
				  String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
				  @Nullable String senderName, @Nullable String references);
	
	void sendMail(SmtpSetting smtpSetting, Collection<String> toList, Collection<String> ccList,
				  Collection<String> bccList, String subject, String htmlBody, String textBody,
				  @Nullable String replyAddress, @Nullable String senderName, String systemAddress, 
				  @Nullable String references);
	
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
	
	Future<?> monitorInbox(ImapSetting imapSetting, String systemAddress,
						   Consumer<Message> messageConsumer, 
						   MailPosition lastPosition, boolean testMode);

	void handleMessage(Message message, String systemAddress, boolean onlyMonitorSystemAddress);
	
}

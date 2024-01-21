package io.onedev.server.model.support.administration.mailservice;

import io.onedev.server.annotation.Editable;
import io.onedev.server.mail.InboxMonitor;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Future;

@Editable
public interface MailService extends Serializable {

	String getSystemAddress();
	
	void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList,
				  String subject, String htmlBody, String textBody, @Nullable String replyAddress,
				  @Nullable String senderName, @Nullable String references);
	
	@Nullable
	InboxMonitor getInboxMonitor();
	
}

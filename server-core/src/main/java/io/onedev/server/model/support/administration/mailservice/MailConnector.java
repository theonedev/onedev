package io.onedev.server.model.support.administration.mailservice;

import java.io.Serializable;
import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.mail.InboxMonitor;

@Editable
public interface MailConnector extends Serializable {

	String getSystemAddress();
	
	void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList,
				  String subject, String htmlBody, String textBody, @Nullable String replyAddress,
				  @Nullable String senderName, @Nullable String references, boolean testMode);

	@Nullable
	InboxMonitor getInboxMonitor(boolean testMode);

}

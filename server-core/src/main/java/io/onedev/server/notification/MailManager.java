package io.onedev.server.notification;

import java.util.Collection;

import io.onedev.server.model.support.administration.MailSetting;

public interface MailManager {
	
	void sendMail(Collection<String> toList, String subject, String htmlBody, String textBody);
	
	void sendMail(MailSetting mailSetting, Collection<String> toList, String subject, 
			String htmlBody, String textBody);
	
	void sendMailAsync(Collection<String> toList, String subject, String htmlBody, String textBody);
	
}

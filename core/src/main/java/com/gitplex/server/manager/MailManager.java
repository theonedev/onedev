package com.gitplex.server.manager;

import java.util.Collection;

import com.gitplex.server.model.support.setting.MailSetting;

public interface MailManager {
	
	void sendMail(Collection<String> toList, String subject, String body);
	
	void sendMail(MailSetting mailSetting, Collection<String> toList, String subject, String body);
	
	void sendMailAsync(Collection<String> toList, String subject, String body);
	
}

package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.setting.MailSetting;

public interface MailManager {
	
	void sendMail(Collection<String> toList, String subject, String body);
	
	void sendMail(MailSetting mailSetting, Collection<String> toList, String subject, String body);
	
	void sendMailAsync(Collection<String> toList, String subject, String body);
	
}

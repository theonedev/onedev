package com.gitplex.core.manager;

import java.util.Collection;

import com.gitplex.core.entity.Account;
import com.gitplex.core.setting.MailSetting;

public interface MailManager {
	
	void sendMail(Collection<Account> toList, String subject, String body);
	
	void sendMail(MailSetting mailSetting, Collection<Account> toList, String subject, String body);
	
	void sendMailAsync(Collection<Account> toList, String subject, String body);
	
}

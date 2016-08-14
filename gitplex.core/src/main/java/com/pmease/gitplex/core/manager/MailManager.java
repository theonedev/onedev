package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.setting.MailSetting;

public interface MailManager {
	
	void sendMail(Collection<Account> toList, String subject, String body);
	
	void sendMail(MailSetting mailSetting, Collection<Account> toList, String subject, String body);
	
	void sendMailAsync(Collection<Account> toList, String subject, String body);
	
}

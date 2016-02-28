package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.setting.MailSetting;

public interface MailManager {
	
	void sendMailNow(@Nullable MailSetting mailSetting, Collection<Account> toList, String subject, String body);
	
	void sendMail(Collection<Account> toList, String subject, String body);
	
}

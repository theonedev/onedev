package com.pmease.gitplex.core.manager;

import java.util.Collection;

import com.pmease.gitplex.core.model.User;

public interface MailManager {
	
	void sendMail(Collection<User> toList, String subject, String body);

	void sendMail(User toUser, String subject, String body);
	
}

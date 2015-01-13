package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultMailManager implements MailManager {

	private final ConfigManager configManager;
	
	@Inject
	public DefaultMailManager(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public void sendMail(Collection<User> toList, String subject, String body) {
		System.out.println("sending email to: " + toList + ", subject: " + subject);
	}

	@Override
	public void sendMail(User toUser, String subject, String body) {
		sendMail(Sets.newHashSet(toUser), subject, body);
	}

}

package com.pmease.gitplex.core.manager.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
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
	public void sendMail(List<User> toList, String subject, String body) {
		System.out.println("sending email to: " + toList + ", subject: " + subject);
	}

	@Override
	public void sendMail(User toUser, String subject, String body) {
		sendMail(Lists.newArrayList(toUser), subject, body);
	}

}

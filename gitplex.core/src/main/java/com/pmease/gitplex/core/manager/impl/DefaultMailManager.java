package com.pmease.gitplex.core.manager.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.setting.MailSetting;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final ConfigManager configManager;
	
	private final ExecutorService executorService;
	
	@Inject
	public DefaultMailManager(ConfigManager configManager, ExecutorService executorService) {
		this.configManager = configManager;
		this.executorService = executorService;
	}
	
	@Override
	public void sendMail(final Collection<User> toList, final String subject, final String body) {
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				try {
					sendMailNow(null, toList, subject, body);
				} catch (Exception e) {
					logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
				}		
			}
			
		});
	}

	@Override
	public void sendMailNow(MailSetting mailSetting, Collection<User> toList, String subject, String body) {
		Collection<String> toAddresses = new ArrayList<>();
		for (User user: toList) {
			if (user.getEmail() != null)
				toAddresses.add(user.getEmail());
		}
		
		if (toAddresses.isEmpty())
			return;

		if (mailSetting == null)
			mailSetting = configManager.getMailSetting();
		
		if (mailSetting == null)
			throw new RuntimeException("Mail setting is not defined.");
	
		HtmlEmail email = new HtmlEmail();
        email.setSocketConnectionTimeout(Bootstrap.SOCKET_CONNECT_TIMEOUT);

        if (mailSetting.getTimeout() != 0)
        	email.setSocketTimeout(mailSetting.getTimeout()*1000);
        
        if (mailSetting.isEnableStartTLS())
        	email.setStartTLSEnabled(true);
        
        email.setSSLCheckServerIdentity(false);
		
		String senderEmail = mailSetting.getSenderAddress();
		if (senderEmail == null) {
			String hostName;
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
			senderEmail = "gitplex@" + hostName;
		}
		try {
			email.setFrom(senderEmail);
			if (mailSetting.getReplyAddress() != null)
				email.addReplyTo(mailSetting.getReplyAddress());
			
			for (String address: toAddresses)
				email.addTo(address);
	
			email.setHostName(mailSetting.getSmtpHost());
			email.setSmtpPort(mailSetting.getSmtpPort());
			email.setSslSmtpPort(String.valueOf(mailSetting.getSslSmtpPort()));
	        email.setSSLOnConnect(mailSetting.isSmtpOverSSL());
	        String smtpUser = mailSetting.getSmtpUser();
			if (smtpUser != null)
				email.setAuthentication(smtpUser, mailSetting.getSmtpPassword());
			email.setCharset(CharEncoding.UTF_8);
			
			email.setSubject(subject);
			email.setHtmlMsg(body);
			
			logger.debug("Sending email (to: {}, subject: {})... " + toAddresses, subject);
			email.send();
		} catch (EmailException e) {
			throw new RuntimeException(e);
		}
	}

}

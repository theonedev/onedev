package com.gitplex.core.manager.impl;

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
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.core.entity.Account;
import com.gitplex.core.manager.ConfigManager;
import com.gitplex.core.manager.MailManager;
import com.gitplex.core.setting.MailSetting;
import com.gitplex.commons.bootstrap.Bootstrap;
import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.dao.Dao;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final ConfigManager configManager;
	
	private final ExecutorService executorService;
	
	private final Dao dao;
	
	@Inject
	public DefaultMailManager(Dao dao, ConfigManager configManager, ExecutorService executorService) {
		this.dao = dao;
		this.configManager = configManager;
		this.executorService = executorService;
	}

	@Sessional
	@Override
	public void sendMailAsync(Collection<Account> toList, String subject, String body) {
		if (dao.getSession().getTransaction().getStatus() == TransactionStatus.ACTIVE) {
			dao.doAsyncAfterCommit(newSendMailRunnable(toList, subject, body));
		} else {
			executorService.execute(newSendMailRunnable(toList, subject, body));
		}
	}
	
	private Runnable newSendMailRunnable(Collection<Account> toList, String subject, String body) {
		return new Runnable() {

			@Override
			public void run() {
				try {
					sendMail(toList, subject, body);
				} catch (Exception e) {
					logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
				}		
			}
			
		};
	}

	@Override
	public void sendMail(MailSetting mailSetting, Collection<Account> toList, String subject, String body) {
		Collection<String> toAddresses = new ArrayList<>();
		for (Account user: toList) {
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
			for (String address: toAddresses)
				email.addTo(address);
	
			email.setHostName(mailSetting.getSmtpHost());
			email.setSmtpPort(mailSetting.getSmtpPort());
			email.setSslSmtpPort(String.valueOf(mailSetting.getSmtpPort()));
	        String smtpUser = mailSetting.getSmtpUser();
			if (smtpUser != null)
				email.setAuthentication(smtpUser, mailSetting.getSmtpPassword());
			email.setCharset(CharEncoding.UTF_8);
			
			email.setSubject(subject);
			email.setHtmlMsg(body);
			
			logger.debug("Sending email (to: {}, subject: {})... ", toAddresses, subject);
			email.send();
		} catch (EmailException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendMail(Collection<Account> toList, String subject, String body) {
		sendMail(configManager.getMailSetting(), toList, subject, body);
	}

}

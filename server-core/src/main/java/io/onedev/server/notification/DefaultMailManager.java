package io.onedev.server.notification;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ExecutorService executorService;
	
	@Inject
	public DefaultMailManager(TransactionManager transactionManager, SettingManager setingManager, 
			ExecutorService executorService) {
		this.transactionManager = transactionManager;
		this.settingManager = setingManager;
		this.executorService = executorService;
	}

	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, String subject, String htmlBody, String textBody) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							sendMail(toList, subject, htmlBody, textBody);
						} catch (Exception e) {
							logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
						}		
					}
					
				});
			}
			
		});
	}

	@Override
	public void sendMail(MailSetting mailSetting, Collection<String> toList, String subject, 
			String htmlBody, String textBody) {
		if (toList.isEmpty())
			return;

		if (mailSetting == null)
			mailSetting = settingManager.getMailSetting();
		
		if (mailSetting != null) {
			Email email;
			
			try {
				if (mailSetting.isSendAsHtml())
					email = new HtmlEmail().setHtmlMsg(htmlBody);
				else
					email = new SimpleEmail().setMsg(textBody);
			} catch (EmailException e) {
				throw new RuntimeException(e);
			}
			
	        email.setSocketConnectionTimeout(Bootstrap.SOCKET_CONNECT_TIMEOUT);

	        if (mailSetting.getTimeout() != 0)
	        	email.setSocketTimeout(mailSetting.getTimeout()*1000);
	        
	        email.setStartTLSEnabled(mailSetting.isEnableStartTLS());
	        email.setSSLOnConnect(false);
	        email.setSSLCheckServerIdentity(false);
			
			String senderEmail = mailSetting.getSenderAddress();
			if (senderEmail == null) {
				String hostName;
				try {
					hostName = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					hostName = "localhost";
				}
				senderEmail = "onedev@" + hostName;
			}
			try {
				email.setFrom(senderEmail);
				for (String address: toList)
					email.addTo(address);
		
				email.setHostName(mailSetting.getSmtpHost());
				email.setSmtpPort(mailSetting.getSmtpPort());
				email.setSslSmtpPort(String.valueOf(mailSetting.getSmtpPort()));
		        String smtpUser = mailSetting.getSmtpUser();
				if (smtpUser != null)
					email.setAuthentication(smtpUser, mailSetting.getSmtpPassword());
				email.setCharset(CharEncoding.UTF_8);
				
				email.setSubject(subject);
				
				logger.debug("Sending email (to: {}, subject: {})... ", toList, subject);
				email.send();
			} catch (EmailException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.warn("Unable to send mail as mail setting is not specified");
		}
	}

	@Override
	public void sendMail(Collection<String> toList, String subject, String htmlBody, String textBody) {
		sendMail(settingManager.getMailSetting(), toList, subject, htmlBody, textBody);
	}

}

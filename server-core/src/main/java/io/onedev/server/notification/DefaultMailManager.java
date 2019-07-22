package io.onedev.server.notification;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.setting.MailSetting;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final SettingManager configManager;
	
	private final TransactionManager transactionManager;
	
	@Inject
	public DefaultMailManager(TransactionManager transactionManager, SettingManager configManager) {
		this.transactionManager = transactionManager;
		this.configManager = configManager;
	}

	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, String subject, String body) {
		transactionManager.runAsyncAfterCommit(new Runnable() {

			@Override
			public void run() {
				try {
					sendMail(toList, subject, body);
				} catch (Exception e) {
					logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
				}		
			}
			
		});
	}

	@Override
	public void sendMail(MailSetting mailSetting, Collection<String> toList, String subject, String body) {
		if (toList.isEmpty())
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
        email.setSSLOnConnect(mailSetting.isEnableSSL());
        email.setSSLCheckServerIdentity(false);
		
		String senderEmail = mailSetting.getSenderAddress();
		if (senderEmail == null) {
			String hostName;
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
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
			email.setHtmlMsg(body);
			
			logger.debug("Sending email (to: {}, subject: {})... ", toList, subject);
			email.send();
		} catch (EmailException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendMail(Collection<String> toList, String subject, String body) {
		sendMail(configManager.getMailSetting(), toList, subject, body);
	}

}

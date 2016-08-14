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

import com.google.common.collect.Sets;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.mention.AccountMentionedInCodeComment;
import com.pmease.gitplex.core.event.mention.AccountMentionedInPullRequest;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.setting.MailSetting;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final ConfigManager configManager;
	
	private final MarkdownManager markdownManager;

	private final ExecutorService executorService;
	
	private final UrlManager urlManager;
	
	@Inject
	public DefaultMailManager(ConfigManager configManager, ExecutorService executorService,
			MarkdownManager markdownManager, UrlManager urlManager) {
		this.configManager = configManager;
		this.executorService = executorService;
		this.markdownManager = markdownManager;
		this.urlManager = urlManager;
	}
	
	@Override
	public void sendMailAsync(Collection<Account> toList, String subject, String body) {
		executorService.execute(new Runnable() {

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

	@Listen
	public void on(AccountMentionedInPullRequest event) {
		PullRequest request = event.getRequest();
		Account user = event.getUser();
		String subject = String.format("You are mentioned in pull request #%d (%s)", 
				request.getNumber(), request.getTitle());
		String url = urlManager.urlFor(request);
		String body = String.format("%s."
				+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
				+ "<p style='margin: 16px 0;'>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				subject, markdownManager.escape(event.getMarkdown()), url, url);
		
		sendMailAsync(Sets.newHashSet(user), subject, decorate(user, body));
	}

	@Listen
	public void on(AccountMentionedInCodeComment event) {
		Account user = event.getUser();
		CodeComment comment = event.getComment();
		String subject = String.format("You are mentioned in code comment #%d (%s)", 
				comment.getId(), comment.getTitle());
		String url = urlManager.urlFor(comment);
		String body = String.format("%s."
				+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
				+ "<p style='margin: 16px 0;'>"
				+ "For details, please visit <a href='%s'>%s</a>", 
				subject, markdownManager.escape(event.getMarkdown()), url, url);
		
		sendMailAsync(Sets.newHashSet(user), subject, decorate(user, body));
	}

	private String decorate(Account user, String body) {
		return String.format("Dear %s, "
				+ "<p style='margin: 16px 0;'>"
				+ "%s"
				+ "<p style='margin: 16px 0;'>"
				+ "-- Sent by GitPlex", 
				user.getDisplayName(), body);
	}
	
}

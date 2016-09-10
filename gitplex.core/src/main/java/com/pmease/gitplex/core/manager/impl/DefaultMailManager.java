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
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.markdown.MarkdownManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.event.MarkdownAware;
import com.pmease.gitplex.core.event.codecomment.CodeCommentEvent;
import com.pmease.gitplex.core.event.codecomment.CodeCommentReplied;
import com.pmease.gitplex.core.event.codecomment.CodeCommentResolved;
import com.pmease.gitplex.core.event.codecomment.CodeCommentUnresolved;
import com.pmease.gitplex.core.event.pullrequest.PullRequestChangeEvent;
import com.pmease.gitplex.core.event.pullrequest.PullRequestCommented;
import com.pmease.gitplex.core.event.pullrequest.PullRequestOpened;
import com.pmease.gitplex.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.MailManager;
import com.pmease.gitplex.core.manager.UrlManager;
import com.pmease.gitplex.core.setting.MailSetting;
import com.pmease.gitplex.core.util.markdown.MentionParser;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final ConfigManager configManager;
	
	private final MarkdownManager markdownManager;

	private final ExecutorService executorService;
	
	private final UrlManager urlManager;
	
	private final Dao dao;
	
	@Inject
	public DefaultMailManager(Dao dao, ConfigManager configManager, ExecutorService executorService,
			MarkdownManager markdownManager, UrlManager urlManager) {
		this.dao = dao;
		this.configManager = configManager;
		this.executorService = executorService;
		this.markdownManager = markdownManager;
		this.urlManager = urlManager;
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

	@Listen
	public void on(PullRequestChangeEvent event) {
		if (event instanceof PullRequestOpened 
				|| event instanceof PullRequestCommented
				|| event instanceof PullRequestStatusChangeEvent) {
			String markdown = ((MarkdownAware) event).getMarkdown();
			if (markdown != null) {
				String url;
				if (event instanceof PullRequestCommented)
					url = urlManager.urlFor(((PullRequestCommented)event).getComment());
				else if (event instanceof PullRequestStatusChangeEvent) 
					url = urlManager.urlFor(((PullRequestStatusChangeEvent)event).getStatusChange());
				else 
					url = urlManager.urlFor(event.getRequest());
				for (Account user: new MentionParser().parseMentions(markdownManager.parse(markdown))) {
					PullRequest request = event.getRequest();
					String subject = String.format("You are mentioned in pull request #%d (%s)", 
							request.getNumber(), request.getTitle());
					String body = String.format("%s."
							+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
							+ "<p style='margin: 16px 0;'>"
							+ "For details, please visit <a href='%s'>%s</a>", 
							subject, markdownManager.escape(markdown), url, url);
					
					sendMailAsync(Sets.newHashSet(user), subject, decorate(user, body));
				}
			}
		}
	}

	@Listen
	public void on(CodeCommentEvent event) {
		if (event.getMarkdown() != null) {
			for (Account user: new MentionParser().parseMentions(markdownManager.parse(event.getMarkdown()))) {
				CodeComment comment = event.getComment();
				String subject = String.format("You are mentioned in a code comment (%s)", 
						comment.getTitle());
				String url;
				if (event instanceof CodeCommentResolved)
					url = urlManager.urlFor(((CodeCommentResolved)event).getStatusChange(), event.getRequest());
				else if (event instanceof CodeCommentUnresolved)
					url = urlManager.urlFor(((CodeCommentUnresolved)event).getStatusChange(), event.getRequest());
				else if (event instanceof CodeCommentReplied) 
					url = urlManager.urlFor(((CodeCommentReplied)event).getReply(), event.getRequest());
				else
					url = urlManager.urlFor(comment, event.getRequest());
					
				String body = String.format("%s."
						+ "<p style='margin: 16px 0; padding-left: 16px; border-left: 4px solid #CCC;'>%s"
						+ "<p style='margin: 16px 0;'>"
						+ "For details, please visit <a href='%s'>%s</a>", 
						subject, markdownManager.escape(event.getMarkdown()), url, url);
				
				sendMailAsync(Sets.newHashSet(user), subject, decorate(user, body));
			}
		}
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

package io.onedev.server.notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.mail.imap.IMAPFolder;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.entitymanager.IssueCommentManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.IssueWatchManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.PullRequestCommentManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueWatch;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.Role;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.MailSetting;
import io.onedev.server.model.support.administration.ReceiveMailSetting;
import io.onedev.server.model.support.administration.SenderAuthorization;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.util.EmailAddress;
import io.onedev.server.util.validation.UserNameValidator;

@Singleton
public class DefaultMailManager implements MailManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ProjectManager projectManager;
	
	private final UserAuthorizationManager authorizationManager;
	
	private final IssueManager issueManager;
	
	private final IssueCommentManager issueCommentManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestCommentManager pullRequestCommentManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	private final ExecutorService executorService;
	
	private final UserManager userManager;
	
	private volatile boolean stopping;
	
	private volatile Thread thread;
	
	@Inject
	public DefaultMailManager(TransactionManager transactionManager, SettingManager setingManager, 
			UserManager userManager, ProjectManager projectManager, 
			UserAuthorizationManager authorizationManager, IssueManager issueManager, 
			IssueCommentManager issueCommentManager, IssueWatchManager issueWatchManager, 
			PullRequestManager pullRequestManager, PullRequestCommentManager pullRequestCommentManager, 
			PullRequestWatchManager pullRequestWatchManager, ExecutorService executorService) {
		this.transactionManager = transactionManager;
		this.settingManager = setingManager;
		this.userManager = userManager;
		this.projectManager = projectManager;
		this.authorizationManager = authorizationManager;
		this.issueManager = issueManager;
		this.issueCommentManager = issueCommentManager;
		this.issueWatchManager = issueWatchManager;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestCommentManager = pullRequestCommentManager;
		this.pullRequestWatchManager = pullRequestWatchManager;
		this.executorService = executorService;
	}

	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, Collection<String> ccList, String subject, 
			String htmlBody, String textBody, String replyAddress, String references) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							sendMail(toList, ccList, subject, htmlBody, textBody, replyAddress, references);
						} catch (Exception e) {
							logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
						}		
					}
					
				});
			}
			
		});
	}

	@Override
	public void sendMail(MailSetting mailSetting, Collection<String> toList, Collection<String> ccList, 
			String subject, String htmlBody, String textBody, String replyAddress, String references) {
		if (toList.isEmpty() && ccList.isEmpty())
			return;

		if (mailSetting == null)
			mailSetting = settingManager.getMailSetting();
		
		if (mailSetting != null) {
			HtmlEmail email = new HtmlEmail();
			try {
				email.setHtmlMsg(htmlBody);
				email.setTextMsg(textBody);
			} catch (EmailException e) {
				throw new RuntimeException(e);
			}
			
	        email.setSocketConnectionTimeout(Bootstrap.SOCKET_CONNECT_TIMEOUT);

	        if (mailSetting.getTimeout() != 0)
	        	email.setSocketTimeout(mailSetting.getTimeout()*1000);
	        
	        email.setStartTLSEnabled(mailSetting.isEnableStartTLS());
	        email.setSSLOnConnect(false);
	        email.setSSLCheckServerIdentity(false);
	        if (references != null)
	        	email.addHeader("References", references);
			
			try {
				if (replyAddress != null)
					email.setReplyTo(Lists.newArrayList(InternetAddress.parse(replyAddress)));
				email.setFrom(mailSetting.getEmailAddress());
				for (String address: toList)
					email.addTo(address);
				for (String address: ccList)
					email.addCc(address);
		
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
			} catch (EmailException | AddressException e) {
				throw new RuntimeException(e);
			}
		} else {
			logger.warn("Unable to send mail as mail setting is not specified");
		}
	}

	@Override
	public void sendMail(Collection<String> toList, Collection<String> ccList, String subject, 
			String htmlBody, String textBody, String replyAddress, String references) {
		sendMail(settingManager.getMailSetting(), toList, ccList, subject, htmlBody, 
				textBody, replyAddress, references);
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == Setting.Key.MAIL) {
				transactionManager.runAfterCommit(new Runnable() {

					@Override
					public void run() {
						Thread thread = DefaultMailManager.this.thread;
						if (thread != null)
							thread.interrupt();
					}
					
				});
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	protected void onMessage(MailSetting mailSetting, Message message) {
		try {
			String[] toHeader = message.getHeader("To");
			String[] fromHeader = message.getHeader("From");
			String[] ccHeader = message.getHeader("Cc");
			if (toHeader != null && toHeader.length != 0) {
				if (fromHeader == null || fromHeader.length == 0)
					throw new ExplicitException("Invalid email message: no from address found");
				
				InternetAddress from = InternetAddress.parse(fromHeader[0], true)[0];
				
				SenderAuthorization authorization = mailSetting.getReceiveMailSetting()
						.getSenderAuthorization(from.getAddress());
				if (authorization == null) 
					throw new ExplicitException("Unauthorized sender: " + from.getAddress());
				
				EmailAddress systemAddress = EmailAddress.parse(mailSetting.getEmailAddress());
				
				Collection<Issue> issues = new ArrayList<>();
				Collection<PullRequest> pullRequests = new ArrayList<>();
				Collection<InternetAddress> involved = new ArrayList<>();

				List<InternetAddress> receivers = new ArrayList<>();
				receivers.addAll(Arrays.asList(InternetAddress.parse(toHeader[0], true)));
				
				if (ccHeader != null && ccHeader.length != 0) 
					receivers.addAll(Arrays.asList(InternetAddress.parse(ccHeader[0], true)));
				
				List<String> receiverEmailAddresses = 
						receivers.stream().map(it->it.getAddress()).collect(Collectors.toList());
				for (InternetAddress receiver: receivers) {
					EmailAddress receiverAddress = EmailAddress.parse(receiver.getAddress());
					if (receiverAddress.toString().equals(systemAddress.toString())) {
						String projectName = authorization.getDefaultProject();
						Project project = projectManager.find(projectName);
						if (project == null) {
							String errorMessage = String.format(
									"Default project not found (sender: %s, project: %s)", 
									from.getAddress(), projectName);
							throw new ExplicitException(errorMessage);
						}
						issues.add(openIssue(message, project, from, authorization));
					} else if (receiverAddress.getDomain().equals(systemAddress.getDomain()) 
							&& receiverAddress.getPrefix().startsWith(systemAddress.getPrefix() + "+")) {
						String subAddress = receiverAddress.getPrefix().substring(systemAddress.getPrefix().length()+1);
						String projectName = StringUtils.substringBefore(subAddress, "~");
						if (projectName.equals(MailManager.TEST_SUB_ADDRESSING)) 
							continue;
						Project project = projectManager.find(projectName);
						if (project == null) 
							throw new ExplicitException("Non-existent project in to address: " +  receiverAddress);
						if (!authorization.isProjectAuthorized(project)) 
							throw new ExplicitException("Unauthorized project in to address: " + receiverAddress);
						String remaining = StringUtils.substringAfter(subAddress, "~");
						if (remaining.length() == 0) {
							openIssue(message, project, from, authorization);
						} else if (remaining.startsWith("issue")) {
							remaining = remaining.substring("issue".length());
							Long issueNumber;
							try {
								issueNumber = Long.valueOf(StringUtils.substringBefore(remaining, "~"));
							} catch (NumberFormatException e) { 
								throw new ExplicitException("Invalid issue number in to address: " + receiverAddress);
							}
							Issue issue = issueManager.find(project, issueNumber);
							if (issue == null)
								throw new ExplicitException("Non-existent issue in to address: " + receiverAddress);
							if (remaining.contains("~")) {
								User user = userManager.findByEmail(from.getAddress());
								if (user != null) {
									IssueWatch watch = issueWatchManager.find(issue, user);
									if (watch != null) {
										watch.setWatching(false);
										issueWatchManager.save(watch);
										String subject = "Unsubscribed successfully from issue " + issue.getFQN(); 
										String body = "You will no longer receive notifications of issue " + issue.getFQN() + " unless mentioned. "
												+ "However if you subscribed to certain issue queries, you may still get notifications of newly "
												+ "created issues matching those queries. In this case, you will need to login to your account "
												+ "and unsubscribe those queries.";
										sendMailAsync(Lists.newArrayList(from.getAddress()), Lists.newArrayList(), subject, body, body, null, getMessageId(message));
									}
								}
							} else {
								addComment(issue, message, from, receiverEmailAddresses, authorization.getAuthorizedRole());
								issues.add(issue);
							}
						} else if (remaining.startsWith("pullrequest")) {
							remaining = remaining.substring("pullrequest".length());
							Long pullRequestNumber;
							try {
								pullRequestNumber = Long.valueOf(StringUtils.substringBefore(remaining, "~"));
							} catch (NumberFormatException e) { 
								throw new ExplicitException("Invalid pull request number in to address: " + receiverAddress);
							}
							PullRequest pullRequest = pullRequestManager.find(project, pullRequestNumber);
							if (pullRequest == null)
								throw new ExplicitException("Non-existent issue in to address: " + receiverAddress);
							
							if (remaining.contains("~")) {
								User user = userManager.findByEmail(from.getAddress());
								if (user != null) {
									PullRequestWatch watch = pullRequestWatchManager.find(pullRequest, user);
									if (watch != null) {
										watch.setWatching(false);
										pullRequestWatchManager.save(watch);
										String subject = "Unsubscribed successfully from pull request " + pullRequest.getFQN(); 
										String body = "You will no longer receive notifications of pull request " + pullRequest.getFQN() 
												+ " unless mentioned. However if you subscribed to certain pull request queries, you may still "
												+ "get notifications of newly submitted pull request matching those queries. In this case, you "
												+ "will need to login to your account and unsubscribe those queries.";
										sendMailAsync(Lists.newArrayList(from.getAddress()), Lists.newArrayList(), subject, body, body, null, getMessageId(message));
									}
								}
							} else {
								addComment(pullRequest, message, from, receiverEmailAddresses, authorization.getAuthorizedRole());
								pullRequests.add(pullRequest);
							}
						} else {
							throw new ExplicitException("Unknown sub addressing: " + receiverAddress);
						}							
					} else {
						involved.add(receiver);
					}
				}
				
				for (Issue issue: issues) {
					for (InternetAddress each: involved) 
						createUserIfNotExist(each, issue, authorization.getAuthorizedRole());
				}
				for (PullRequest pullRequest: pullRequests) {
					for (InternetAddress each: involved) 
						createUserIfNotExist(each, pullRequest, authorization.getAuthorizedRole());
				}
			}
		} catch (Exception e) {
			logger.error("Error processing incoming email", e);
		} 
	}
	
	private void addComment(Issue issue, Message message, InternetAddress author, 
			Collection<String> receiverEmailAddresses, Role role) throws IOException, MessagingException {
		IssueComment comment = new IssueComment();
		comment.setIssue(issue);
		User user = createUserIfNotExist(author, issue.getProject(), role);
		comment.setUser(user);
		String content = readText(issue.getProject(), issue.getUUID(), message);
		if (StringUtils.isNotBlank(content)) {
			comment.setContent(content);
			issueCommentManager.save(comment, receiverEmailAddresses);
		}
	}
	
	private void addComment(PullRequest pullRequest, Message message, InternetAddress author, 
			Collection<String> receiverEmailAddresses, Role role) throws IOException, MessagingException {
		PullRequestComment comment = new PullRequestComment();
		comment.setRequest(pullRequest);
		User user = createUserIfNotExist(author, pullRequest.getProject(), role);
		comment.setUser(user);
		String content = readText(pullRequest.getProject(), pullRequest.getUUID(), message);
		if (StringUtils.isNotBlank(content)) {
			comment.setContent(content);
			pullRequestCommentManager.save(comment, receiverEmailAddresses);
		}
	}
	
	@Nullable
	private String getMessageId(Message message) throws MessagingException {
		String[] messageId = message.getHeader("Message-ID");
		if (messageId != null && messageId.length != 0)
			return messageId[0];
		else
			return null;
	}
	
	private Issue openIssue(Message message, Project project, InternetAddress submitter, 
			SenderAuthorization authorization) throws MessagingException, IOException {
		Issue issue = new Issue();
		issue.setProject(project);
		if (StringUtils.isNotBlank(message.getSubject()))
			issue.setTitle(message.getSubject());
		else
			issue.setTitle("No title");
		
		String messageId = getMessageId(message);
		if (messageId != null)
			issue.setThreadingReference(messageId);

		String description = readText(project, issue.getUUID(), message);
		if (StringUtils.isNotBlank(description))
			issue.setDescription(description);
		
		User user = createUserIfNotExist(submitter, project, authorization.getAuthorizedRole());
		issue.setSubmitter(user);
		
		GlobalIssueSetting issueSetting = settingManager.getIssueSetting();
		issue.setState(issueSetting.getInitialStateSpec().getName());
		for (FieldSupply supply: authorization.getIssueFields()) {
			Object fieldValue = issueSetting.getFieldSpec(supply.getName())
					.convertToObject(supply.getValueProvider().getValue());
			issue.setFieldValue(supply.getName(), fieldValue);
		}
		issueManager.open(issue);
		return issue;
	}
	
	private User createUserIfNotExist(InternetAddress address, Project project, Role role) {
		User user = userManager.findByEmail(address.getAddress());
		if (user == null) {
			user = new User();
			user.setName(UserNameValidator.suggestUserName(EmailAddress.parse(address.getAddress()).getPrefix()));
			user.setEmail(address.getAddress());
			user.setFullName(address.getPersonal());
			user.setPassword("12345");
			userManager.save(user);
		} 
		
		boolean found = false;
		for (UserAuthorization authorization: user.getAuthorizations()) {
			if (authorization.getProject().equals(project)) {
				found = true;
				break;
			}
		}
		if (!found) {
			UserAuthorization authorization = new UserAuthorization();
			authorization.setUser(user);
			authorization.setProject(project);
			authorization.setRole(role);
			authorizationManager.save(authorization);
		}
		return user;
	}
	
	@Transactional
	private User createUserIfNotExist(InternetAddress address, Issue issue, Role role) {
		User user = createUserIfNotExist(address, issue.getProject(), role);
		boolean found = false;
		for (IssueWatch watch: user.getIssueWatches()) {
			if (watch.getIssue().equals(issue)) {
				found = true;
				break;
			}
		}
		if (!found) {
			IssueWatch watch = new IssueWatch();
			watch.setIssue(issue);
			watch.setUser(user);
			watch.setWatching(true);
			issueWatchManager.save(watch);
		}
		return user;
	}
	
	private User createUserIfNotExist(InternetAddress address, PullRequest pullRequest, Role role) {
		User user = createUserIfNotExist(address, pullRequest.getProject(), role);
		boolean found = false;
		for (PullRequestWatch watch: user.getPullRequestWatches()) {
			if (watch.getRequest().equals(pullRequest)) {
				found = true;
				break;
			}
		}
		if (!found) {
			PullRequestWatch watch = new PullRequestWatch();
			watch.setRequest(pullRequest);
			watch.setUser(user);
			watch.setWatching(true);
			pullRequestWatchManager.save(watch);
		}
		return user;
	}
	
	@Listen
	public void on(SystemStarted event) {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!stopping) {
					try {
						MailSetting mailSetting = settingManager.getMailSetting();
						monitorInbox(mailSetting, new MessageListener() {
	
							@Override
							public void onReceived(Message message) {
								onMessage(mailSetting, message);
							}
							
						}).waitForFinish();
					} catch (Exception e) {
						if (ExceptionUtils.find(e, InterruptedException.class) == null)
							logger.error("Error monitoring inbox", e);
					} finally {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
				}
				thread = null;
			}
		});
		thread.start();
	}
	
	@Listen
	public void on(SystemStopping event) {
		stopping = true;
		while (true) {
			Thread thread = this.thread;
			if (thread != null) {
				thread.interrupt();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}
	}
	
	private void close(AtomicReference<Store> storeRef, AtomicReference<IMAPFolder> inboxRef) {
		if (inboxRef.get() != null) {
			if (inboxRef.get().isOpen()) {
				try {
					inboxRef.get().close(false);
				} catch (Exception e) {
				}
			}
			inboxRef.set(null);
		}
		if (storeRef.get() != null) {
			try {
				storeRef.get().close();
			} catch (Exception e) {
			}
			storeRef.set(null);
		}
	}
	
	@Override
	public InboxMonitor monitorInbox(MailSetting mailSetting, MessageListener listener) {
		if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
			ReceiveMailSetting receiveMailSetting = mailSetting.getReceiveMailSetting();
			
	        Properties properties = new Properties();
	        
	        properties.setProperty("mail.imap.host", receiveMailSetting.getImapHost());
	        properties.setProperty("mail.imap.port", String.valueOf(receiveMailSetting.getImapPort()));
	        properties.setProperty("mail.imap.starttls.enable", String.valueOf(mailSetting.isEnableStartTLS()));        
	 
	        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	        properties.setProperty("mail.imap.socketFactory.fallback", "false");
	        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(receiveMailSetting.getImapPort()));
	        
	        properties.setProperty("mail.imap.connectiontimeout", String.valueOf(Bootstrap.SOCKET_CONNECT_TIMEOUT));
	        if (mailSetting.getTimeout() != 0)
	        	properties.setProperty("mail.imap.timeout", String.valueOf(mailSetting.getTimeout()*1000));
			
	        AtomicReference<IMAPFolder> inboxRef = new AtomicReference<>(null);
	        AtomicReference<Store> store = new AtomicReference<>(null);
			try {
				Session session = Session.getInstance(properties);
				store.set(session.getStore("imap"));
				
				store.get().connect(receiveMailSetting.getImapUser(), receiveMailSetting.getImapPassword());

				inboxRef.set((IMAPFolder) store.get().getFolder("INBOX"));
				inboxRef.get().open(Folder.READ_ONLY);
				inboxRef.get().addMessageCountListener(new MessageCountListener() {
					
					@Override
					public void messagesAdded(MessageCountEvent event) {
						for (Message message: event.getMessages()) 
							listener.onReceived(message);
					}

					@Override
					public void messagesRemoved(MessageCountEvent e) {
					}

				});

				AtomicReference<RuntimeException> exception = new AtomicReference<>(null);
				AtomicReference<Boolean> stopping = new AtomicReference<>(false);
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							while (!stopping.get()) 
								inboxRef.get().idle();
						} catch (Exception e) {
							exception.set(ExceptionUtils.unchecked(e));
						} finally {
							close(store, inboxRef);
						}
					}
					
				});
				return new InboxMonitor() {

					@Override
					public void stop() {
						IMAPFolder inbox = inboxRef.get();
						if (inbox != null) {
							stopping.set(true);
							try {
								inbox.close(false);
							} catch (Exception e) {
							}
						} 
					}

					@Override
					public void waitForFinish() throws InterruptedException {
						InterruptedException interruptedException = null;
						while (inboxRef.get() != null) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								interruptedException = e;
								stop();
							}
						}
						if (interruptedException != null)
							throw interruptedException;
						
						if (exception.get() != null)
							throw exception.get();
					}
					
				};
			} catch (Exception e) {
				close(store, inboxRef);
				throw ExceptionUtils.unchecked(e);
			}
		} else {
			Future<?> future = executorService.submit(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
					}
				}
				
			});
			return new InboxMonitor() {

				@Override
				public void stop() {
					future.cancel(true);
				}

				@Override
				public void waitForFinish() throws InterruptedException {
					try {
						future.get();
					} catch (InterruptedException e) {
						stop();
						throw e;
					} catch (ExecutionException e) {
						throw new RuntimeException(e);
					}
				}
				
			};
		}
	}
	
	@Override
	public String getReplyAddress(Issue issue) {
		MailSetting mailSetting = settingManager.getMailSetting();
		if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
			EmailAddress systemAddress = EmailAddress.parse(mailSetting.getEmailAddress());
			return systemAddress.getPrefix() + "+" + issue.getProject().getName() + "~issue" + issue.getNumber()
					+ "@" + systemAddress.getDomain(); 
		} else {
			return null;
		}
	}

	@Override
	public String getReplyAddress(PullRequest request) {
		MailSetting mailSetting = settingManager.getMailSetting();
		if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
			EmailAddress systemAddress = EmailAddress.parse(mailSetting.getEmailAddress());
			return systemAddress.getPrefix() + "+" + request.getProject().getName() + "~pullrequest" + request.getNumber()
					+ "@" + systemAddress.getDomain(); 
		} else {
			return null;
		}
	}
	
	@Override
	public String getUnsubscribeAddress(Issue issue) {
		MailSetting mailSetting = settingManager.getMailSetting();
		if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
			EmailAddress systemAddress = EmailAddress.parse(mailSetting.getEmailAddress());
			return systemAddress.getPrefix() + "+" + issue.getProject().getName() + "~issue" + issue.getNumber() + "~unsubscribe"
					+ "@" + systemAddress.getDomain(); 
		} else {
			return null;
		}
	}

	@Override
	public String getUnsubscribeAddress(PullRequest request) {
		MailSetting mailSetting = settingManager.getMailSetting();
		if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
			EmailAddress systemAddress = EmailAddress.parse(mailSetting.getEmailAddress());
			return systemAddress.getPrefix() + "+" + request.getProject().getName() + "~pullrequest" + request.getNumber() + "~unsubscribe"
					+ "@" + systemAddress.getDomain(); 
		} else {
			return null;
		}
	}
	
	private String readText(Project project, String attachmentGroup, Message message) 
			throws IOException, MessagingException {
		Attachments attachments = new Attachments();
		fillAttachments(project, attachmentGroup, message, attachments);
		String text = readText(project, attachmentGroup, message, attachments);

		attachments.identifiable.keySet().removeAll(attachments.referenced);
		attachments.nonIdentifiable.addAll(attachments.identifiable.values());
		if (!attachments.nonIdentifiable.isEmpty()) {
			text += "\n\n---";
			List<String> markdowns = new ArrayList<>();
			for (Attachment attachment: attachments.nonIdentifiable)
				markdowns.add(attachment.getMarkdown());
			text += "\n" + Joiner.on(" &nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp; ").join(markdowns);
		}
		return text;
	}
	
	private String readText(Project project, String attachmentGroup, Part part, Attachments attachments) 
			throws IOException, MessagingException {
		if (part.getDisposition() == null) {
		    if (part.isMimeType("text/plain")) {
		        return part.getContent().toString();
		    } else if (part.isMimeType("text/html")) {
		        Document doc = Jsoup.parse(part.getContent().toString());
		        for (Element element: doc.getElementsByTag("img")) {
		        	String src = element.attr("src");
		        	if (src != null && src.startsWith("cid:")) {
		        		String contentId = "<" + src.substring("cid:".length()) + ">";
		        		attachments.referenced.add(contentId);
		        		Attachment attachment = attachments.identifiable.get(contentId);
		        		if (attachment != null) 
		        			element.attr("src", attachment.url);
		        	}
		        }
		        return doc.html();
		    } else if (part.isMimeType("multipart/*")) {
		    	Multipart multipart = (Multipart) part.getContent();
			    int count = multipart.getCount();
			    if (count != 0) {
				    boolean multipartAlt = new ContentType(multipart.getContentType()).match("multipart/alternative");
				    if (multipartAlt)
				        // alternatives appear in an order of increasing 
				        // faithfulness to the original content. Customize as req'd.
				        return readText(project, attachmentGroup, multipart.getBodyPart(count - 1), attachments);
				    String result = "";
				    for (int i=0; i<count; i++)  
				        result += readText(project, attachmentGroup, multipart.getBodyPart(i), attachments);
				    return result;
			    } else {
			    	return "";
			    }
		    } else { 
		    	return "";
		    }
		} else {
			return "";
		}
	}
	
	private void fillAttachments(Project project, String attachmentGroup, Part part, Attachments attachments) 
			throws IOException, MessagingException {
	    if (part.getDisposition() != null) {
	    	String[] contentId = part.getHeader("Content-ID");
	    	String fileName = MimeUtility.decodeText(part.getFileName());
	        String attachmentName = project.saveAttachment(attachmentGroup, fileName, part.getInputStream());
			String attachmentUrl = project.getAttachmentUrlPath(attachmentGroup, attachmentName);
			Attachment attachment;
	        if (part.isMimeType("image/*"))
	        	attachment = new ImageAttachment(attachmentUrl, fileName);
	        else
	        	attachment = new FileAttachment(attachmentUrl, fileName);
			if (contentId != null && contentId.length != 0) 
				attachments.identifiable.put(contentId[0], attachment);
			else 
				attachments.nonIdentifiable.add(attachment);
	    } else if (part.isMimeType("multipart/*")) {
	    	Multipart multipart = (Multipart) part.getContent();
		    int count = multipart.getCount();
		    if (count != 0) {
			    boolean multipartAlt = new ContentType(multipart.getContentType()).match("multipart/alternative");
			    if (multipartAlt)
			        // alternatives appear in an order of increasing 
			        // faithfulness to the original content. Customize as req'd.
			        fillAttachments(project, attachmentGroup, multipart.getBodyPart(count - 1), attachments);
			    for (int i=0; i<count; i++)  
			        fillAttachments(project, attachmentGroup, multipart.getBodyPart(i), attachments);
		    }
	    } 
	}
	
	private static class Attachments {
		
		final Map<String, Attachment> identifiable = new LinkedHashMap<>();
		
		final Collection<Attachment> nonIdentifiable = new ArrayList<>();
		
		final Collection<String> referenced = new HashSet<>();

	}
	
	private static abstract class Attachment {
		
		final String url;
		
		final String fileName;
		
		public Attachment(String url, String fileName) {
			this.url = url;
			this.fileName = fileName;
		}

		public abstract String getMarkdown();
		
	}
	
	private static class ImageAttachment extends Attachment {

		public ImageAttachment(String url, String fileName) {
			super(url, fileName);
		}

		@Override
		public String getMarkdown() {
			return String.format("![%s](%s)", fileName, url);
		}
		
	}
	
	private static class FileAttachment extends Attachment {

		public FileAttachment(String url, String fileName) {
			super(url, fileName);
		}
		
		@Override
		public String getMarkdown() {
			return String.format("[%s](%s)", fileName, url);
		}
		
	}

}

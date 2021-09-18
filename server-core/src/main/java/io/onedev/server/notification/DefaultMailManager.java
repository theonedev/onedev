package io.onedev.server.notification;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.attribute.FileTime;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.UnauthorizedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import edu.emory.mathcs.backport.java.util.Arrays;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.Listen;
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
import io.onedev.server.entitymanager.UrlManager;
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
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.EmailAddress;
import io.onedev.server.util.HtmlUtils;
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
	
	private final UrlManager urlManager;
	
	private volatile Thread thread;
	
	@Inject
	public DefaultMailManager(TransactionManager transactionManager, SettingManager setingManager, 
			UserManager userManager, ProjectManager projectManager, 
			UserAuthorizationManager authorizationManager, IssueManager issueManager, 
			IssueCommentManager issueCommentManager, IssueWatchManager issueWatchManager, 
			PullRequestManager pullRequestManager, PullRequestCommentManager pullRequestCommentManager, 
			PullRequestWatchManager pullRequestWatchManager, ExecutorService executorService, 
			UrlManager urlManager) {
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
		this.urlManager = urlManager;
	}

	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
			String subject, String htmlBody, String textBody, String replyAddress, String references) {
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				executorService.execute(new Runnable() {

					@Override
					public void run() {
						try {
							sendMail(toList, ccList, bccList, subject, htmlBody, textBody, replyAddress, references);
						} catch (Exception e) {
							logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
						}		
					}
					
				});
			}
			
		});
	}
	
	private String getThreadIndex(String references) {
		byte[] threadIndexBytes = new byte[22];
		FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
		long value = ft.to(TimeUnit.MICROSECONDS);
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.mark();
		buffer.putLong(value);
		buffer.reset();
		buffer.get(threadIndexBytes, 0, 6);

		byte[] md5Bytes = DigestUtils.md5(references.toString());
		System.arraycopy(md5Bytes, 0, threadIndexBytes, 6, md5Bytes.length);
		return Base64.encodeBase64String(threadIndexBytes);
	}

	@Override
	public void sendMail(MailSetting mailSetting, Collection<String> toList, Collection<String> ccList, 
			Collection<String> bccList, String subject, String htmlBody, String textBody, 
			String replyAddress, String references) {
		if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty())
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
	        email.setSocketTimeout(mailSetting.getTimeout()*1000);
	        
	        email.setStartTLSEnabled(mailSetting.isEnableStartTLS());
	        email.setSSLOnConnect(false);
	        email.setSSLCheckServerIdentity(false);
	        if (references != null) {
	        	email.addHeader("References", references);
	        	email.addHeader("In-Reply-To", references);
	        	email.addHeader("Thread-Index", getThreadIndex(references));
	        }
			
			try {
				if (replyAddress != null)
					email.setReplyTo(Lists.newArrayList(InternetAddress.parse(replyAddress)));
				email.setFrom(mailSetting.getEmailAddress());
				for (String address: toList)
					email.addTo(address);
				for (String address: ccList)
					email.addCc(address);
				for (String address: bccList)
					email.addBcc(address);
		
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
	public void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
			String subject, String htmlBody, String textBody, String replyAddress, String references) {
		sendMail(settingManager.getMailSetting(), toList, ccList, bccList, subject, htmlBody, 
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
						Thread copy = thread;
						if (copy != null)
							copy.interrupt();
					}
					
				});
			}
		}
	}

	private void checkPermission(InternetAddress sender, Project project, Permission privilege, 
			@Nullable User user, @Nullable SenderAuthorization authorization) {
		if ((user == null || !user.asSubject().isPermitted(new ProjectPermission(project, privilege))) 
				&& (authorization == null || !authorization.isPermitted(project, privilege))) {
			String errorMessage = String.format("Permission denied (project: %s, sender: %s, permission: %s)", 
					project.getName(), sender.getAddress(), privilege.getClass().getName());
			throw new UnauthorizedException(errorMessage);
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

				User user = userManager.findByEmail(from.getAddress());

				SenderAuthorization authorization = null;
				String designatedProject = null;
				ServiceDeskSetting serviceDeskSetting = settingManager.getServiceDeskSetting();
				if (serviceDeskSetting != null) {
					authorization = serviceDeskSetting.getSenderAuthorization(from.getAddress());
					designatedProject = serviceDeskSetting.getDesignatedProject(from.getAddress());
				} 
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
						if (serviceDeskSetting != null) {
							Project project = projectManager.find(designatedProject);
							if (project == null) {
								String errorMessage = String.format(
										"Sender project does not exist (sender: %s, project: %s)", 
										from.getAddress(), designatedProject);
								throw new ExplicitException(errorMessage);
							}
							checkPermission(from, project, new AccessProject(), user, authorization);
							issues.add(openIssue(message, project, from, user, authorization));
						} else {
							throw new ExplicitException("Unable to create issue from email as service desk is not enabled");
						}
					} else if (receiverAddress.getDomain().equals(systemAddress.getDomain()) 
							&& receiverAddress.getPrefix().startsWith(systemAddress.getPrefix() + "+")) {
						String subAddress = receiverAddress.getPrefix().substring(systemAddress.getPrefix().length()+1);
						String projectName = StringUtils.substringBefore(subAddress, "~");
						if (projectName.equals(MailManager.TEST_SUB_ADDRESSING)) 
							continue;
						Project project = projectManager.find(projectName);
						if (project == null) 
							throw new ExplicitException("Non-existent project specified in receipient address: " +  receiverAddress);
						
						String remaining = StringUtils.substringAfter(subAddress, "~");
						if (remaining.length() == 0) {
							if (serviceDeskSetting != null) {
								checkPermission(from, project, new AccessProject(), user, authorization);
								issues.add(openIssue(message, project, from, user, authorization));
							} else {
								throw new ExplicitException("Unable to create issue from email as service desk is not enabled");
							}
						} else if (remaining.startsWith("issue")) {
							remaining = remaining.substring("issue".length());
							Long issueNumber;
							try {
								issueNumber = Long.valueOf(StringUtils.substringBefore(remaining, "~"));
							} catch (NumberFormatException e) { 
								throw new ExplicitException("Invalid issue number specified in receipient address: " + receiverAddress);
							}
							Issue issue = issueManager.find(project, issueNumber);
							if (issue == null)
								throw new ExplicitException("Non-existent issue specified in receipient address: " + receiverAddress);
							if (remaining.contains("~")) {
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
										sendMailAsync(Lists.newArrayList(from.getAddress()), Lists.newArrayList(), Lists.newArrayList(), 
												subject, body, body, null, getMessageId(message));
									}
								}
							} else {
								checkPermission(from, project, new AccessProject(), user, authorization);
								addComment(issue, message, from, receiverEmailAddresses, user, authorization);
								issues.add(issue);
							}
						} else if (remaining.startsWith("pullrequest")) {
							remaining = remaining.substring("pullrequest".length());
							Long pullRequestNumber;
							try {
								pullRequestNumber = Long.valueOf(StringUtils.substringBefore(remaining, "~"));
							} catch (NumberFormatException e) { 
								throw new ExplicitException("Invalid pull request number specified in receipient address: " + receiverAddress);
							}
							PullRequest pullRequest = pullRequestManager.find(project, pullRequestNumber);
							if (pullRequest == null)
								throw new ExplicitException("Non-existent pull request specified in receipient address: " + receiverAddress);
							
							if (remaining.contains("~")) {
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
										sendMailAsync(Lists.newArrayList(from.getAddress()), Lists.newArrayList(), Lists.newArrayList(), 
												subject, body, body, null, getMessageId(message));
									}
								}
							} else {
								checkPermission(from, project, new ReadCode(), user, authorization);
								addComment(pullRequest, message, from, receiverEmailAddresses, user, authorization);
								pullRequests.add(pullRequest);
							}
						} else {
							throw new ExplicitException("Invalid receipient address: " + receiverAddress);
						}							
					} else {
						involved.add(receiver);
					}
				}
				
				for (Issue issue: issues) {
					for (InternetAddress each: involved) {
						user = userManager.findByEmail(each.getAddress());
						if (serviceDeskSetting != null)
							authorization = serviceDeskSetting.getSenderAuthorization(each.getAddress());
						try {
							checkPermission(each, issue.getProject(), new AccessProject(), user, authorization);
							if (user == null) 
								user = createUserIfNotExist(each, issue.getProject(), authorization.getAuthorizedRole());
							watch(user, issue);
						} catch (UnauthorizedException e) {
							logger.error("Error adding receipient to watch list", e);
						}
					}
				}
				for (PullRequest pullRequest: pullRequests) {
					for (InternetAddress each: involved) { 
						user = userManager.findByEmail(each.getAddress());
						if (serviceDeskSetting != null)
							authorization = serviceDeskSetting.getSenderAuthorization(each.getAddress());
						try {
							checkPermission(each, pullRequest.getProject(), new ReadCode(), user, authorization);
							if (user == null) 
								user = createUserIfNotExist(each, pullRequest.getProject(), authorization.getAuthorizedRole());
							watch(user, pullRequest);
						} catch (UnauthorizedException e) {
							logger.error("Error adding receipient to watch list", e);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error processing incoming email", e);
		} 
	}
	
	private void removeNodesAfter(Node node) {
		Node current = node;
		while (current != null) {
			Node nextSibling = current.nextSibling();
			while (nextSibling != null) {
				Node temp = nextSibling.nextSibling();
				nextSibling.remove();
				nextSibling = temp;
			}
			current = current.parent();
		}
	}
	
	@Nullable
	private String stripQuotation(String content) {
		String quotedSender = settingManager.getMailSetting().getEmailAddress();
		Pattern pattern = Pattern.compile("(^|\\W)" + quotedSender.replace(".", "\\.") + "($|\\W)");
		
		Document document = HtmlUtils.parse(content);
		Element quotedSenderElement = null;
		for (Element element: document.getElementsContainingOwnText(quotedSender)) {
			if (pattern.matcher(element.text()).find()) {
				quotedSenderElement = element;
				break;
			}
		}
		if (quotedSenderElement != null) {
			Element quotedSenderBlockElement = quotedSenderElement.parent();
			while (quotedSenderBlockElement != null 
					&& !quotedSenderBlockElement.tagName().equals("div") 
					&& !quotedSenderBlockElement.tagName().equals("p")) {
				quotedSenderBlockElement = quotedSenderBlockElement.parent();
			}
			if (quotedSenderBlockElement != null) {
				removeNodesAfter(quotedSenderBlockElement);
				quotedSenderBlockElement.remove();
			}
		}
		
		AtomicReference<Node> lastContentNodeRef = new AtomicReference<>(null);
		
		NodeTraversor.traverse(new NodeVisitor() {
			
			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Element && ((Element) node).tagName().equals("img") 
						|| node instanceof TextNode && StringUtils.isNotBlank(((TextNode) node).getWholeText())) {  
					lastContentNodeRef.set(node);
				}
			}
			
			@Override
			public void head(Node node, int depth) {
				
			}
			
		}, document);

		Node lastContentNode = lastContentNodeRef.get();
		if (lastContentNode != null) {
			removeNodesAfter(lastContentNode);
			return document.body().html();
		} else {
			return null;
		}
	}
	
	private void addComment(Issue issue, Message message, InternetAddress author, 
			Collection<String> receiverEmailAddresses, @Nullable User user, 
			@Nullable SenderAuthorization authorization) throws IOException, MessagingException {
		IssueComment comment = new IssueComment();
		comment.setIssue(issue);
		if (user == null)
			user = createUserIfNotExist(author, issue.getProject(), authorization.getAuthorizedRole());
		comment.setUser(user);
		String content = stripQuotation(readText(issue.getProject(), issue.getUUID(), message));
		if (content != null) {
			comment.setContent(content);
			issueCommentManager.save(comment, receiverEmailAddresses);
		}
	}
	
	private void addComment(PullRequest pullRequest, Message message, InternetAddress author, 
			Collection<String> receiverEmailAddresses, @Nullable User user, 
			@Nullable SenderAuthorization authorization) throws IOException, MessagingException {
		PullRequestComment comment = new PullRequestComment();
		comment.setRequest(pullRequest);
		if (user == null)
			user = createUserIfNotExist(author, pullRequest.getProject(), authorization.getAuthorizedRole());
		comment.setUser(user);
		String content = stripQuotation(readText(pullRequest.getProject(), pullRequest.getUUID(), message));
		if (content != null) {
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
			@Nullable User user, @Nullable SenderAuthorization authorization) throws MessagingException, IOException {
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

		if (user == null)
			user = createUserIfNotExist(submitter, project, authorization.getAuthorizedRole());
		issue.setSubmitter(user);
		
		GlobalIssueSetting issueSetting = settingManager.getIssueSetting();
		issue.setState(issueSetting.getInitialStateSpec().getName());
		
		List<FieldSupply> issueFields = settingManager.getServiceDeskSetting()
				.getIssueCreationSetting(submitter.getAddress(), project);
		for (FieldSupply supply: issueFields) {
			Object fieldValue = issueSetting.getFieldSpec(supply.getName())
					.convertToObject(supply.getValueProvider().getValue());
			issue.setFieldValue(supply.getName(), fieldValue);
		}
		
		issueManager.open(issue);
		
		String htmlBody = String.format("Issue <a href='%s'>%s</a> is created. You may reply this email to add more comments", 
				urlManager.urlFor(issue), issue.getFQN());
		String textBody = String.format("Issue %s is created. You may reply this email to add more comments", 
				issue.getFQN());
		
		sendMailAsync(Lists.newArrayList(submitter.getAddress()), Lists.newArrayList(), Lists.newArrayList(),
				"Re: " + issue.getTitle(), htmlBody, textBody, getReplyAddress(issue), 
				issue.getEffectiveThreadingReference()); 
		return issue;
	}
	
	private User createUserIfNotExist(InternetAddress address, Project project, Role role) {
		User user = userManager.findByEmail(address.getAddress());
		if (user == null) {
			user = new User();
			user.setName(UserNameValidator.suggestUserName(EmailAddress.parse(address.getAddress()).getPrefix()));
			user.setEmail(address.getAddress());
			user.setFullName(address.getPersonal());
			user.setPassword("impossible password");
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
	
	private void watch(User user, Issue issue) {
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
	}
	
	private void watch(User user, PullRequest request) {
		boolean found = false;
		for (PullRequestWatch watch: user.getPullRequestWatches()) {
			if (watch.getRequest().equals(request)) {
				found = true;
				break;
			}
		}
		if (!found) {
			PullRequestWatch watch = new PullRequestWatch();
			watch.setRequest(request);
			watch.setUser(user);
			watch.setWatching(true);
			pullRequestWatchManager.save(watch);
		}
	}
	
	@Listen
	public void on(SystemStarted event) {
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (thread != null) {
					try {
						MailSetting mailSetting = settingManager.getMailSetting();
						if (mailSetting != null && mailSetting.getReceiveMailSetting() != null) {
							Future<?> future = monitorInbox(mailSetting.getReceiveMailSetting(), mailSetting.isEnableStartTLS(), 
									mailSetting.getTimeout(), new MessageListener() {
								
								@Override
								public void onReceived(Message message) {
									onMessage(mailSetting, message);
								}
								
							});
							try {
								future.get();
							} catch (InterruptedException e) {
								future.cancel(true);
							} catch (ExecutionException e) {
								logger.error("Error monitoring inbox", e);
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e2) {
								}
							}
						} else {
							try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
							}
						}
					} catch (Exception e) {
						logger.error("Error checking mail setting", e);
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e2) {
						}
					}
				}
			}
		});
		thread.start();
	}
	
	@Listen
	public void on(SystemStopping event) {
		Thread copy = thread;
		thread = null;
		if (copy != null) {
			copy.interrupt();
			try {
				copy.join();
			} catch (InterruptedException e) {
			}
		}
	}
	
	@Override
	public Future<?> monitorInbox(ReceiveMailSetting receiveMailSetting, boolean enableStartTLS, 
			int timeout, MessageListener listener) {
		return executorService.submit(new Runnable() {

			@Override
			public void run() {
		        Properties properties = new Properties();
		        
		        properties.setProperty("mail.imap.host", receiveMailSetting.getImapHost());
		        properties.setProperty("mail.imap.port", String.valueOf(receiveMailSetting.getImapPort()));
		        properties.setProperty("mail.imap.starttls.enable", String.valueOf(enableStartTLS));        
		 
		        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		        properties.setProperty("mail.imap.socketFactory.fallback", "false");
		        properties.setProperty("mail.imap.socketFactory.port", String.valueOf(receiveMailSetting.getImapPort()));
		        
		        properties.setProperty("mail.imap.connectiontimeout", String.valueOf(Bootstrap.SOCKET_CONNECT_TIMEOUT));
		        properties.setProperty("mail.imap.timeout", String.valueOf(timeout*1000));
				
				Session session = Session.getInstance(properties);
				Store store = null;
				IMAPFolder inbox = null;
				Future<?> future = null;
				try {
					store = session.getStore("imap");
					store.connect(receiveMailSetting.getImapUser(), receiveMailSetting.getImapPassword());
					inbox = (IMAPFolder) store.getFolder("INBOX");
					inbox.open(Folder.READ_ONLY);
					inbox.addMessageCountListener(new MessageCountListener() {
						
						@Override
						public void messagesAdded(MessageCountEvent event) {
							for (Message message: event.getMessages()) 
								listener.onReceived(message);
						}

						@Override
						public void messagesRemoved(MessageCountEvent e) {
						}

					});

					IMAPFolder inboxCopy = inbox;
					future = executorService.submit(new Runnable() {

						@Override
						public void run() {
							while (!Thread.interrupted()) { 
								try {
									inboxCopy.idle();
								} catch (MessagingException e) {
									throw new RuntimeException(e);
								}
							}
						}
						
					});
					
					while (true) {
						Thread.sleep(timeout*1000/2);
		                inbox.doCommand(new IMAPFolder.ProtocolCommand() {
		                	
		                    public Object doCommand(IMAPProtocol p) throws ProtocolException {
		                        p.simpleCommand("NOOP", null);
		                        return null;
		                    }
		                    
		                });						
					}
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				} finally {
					if (future != null)
						future.cancel(true);
					if (inbox != null && inbox.isOpen()) {
						try {
							inbox.close(false);
						} catch (Exception e) {
						}
					}
					if (store != null) {
						try {
							store.close();
						} catch (Exception e) {
						}
					}
				}
			}
		});
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

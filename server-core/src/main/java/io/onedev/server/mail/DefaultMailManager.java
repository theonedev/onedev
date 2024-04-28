package io.onedev.server.mail;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.ibm.icu.impl.locale.XCldrStub.Splitter;
import com.sun.mail.imap.IMAPFolder;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.*;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.*;
import io.onedev.server.model.support.administration.*;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.issue.field.instance.FieldInstance;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.validation.validator.UserNameValidator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Safelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unbescape.html.HtmlEscape;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.Setting.Key.MAIL_SERVICE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultMailManager implements MailManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailManager.class);

	private static final int SOCKET_CONNECT_TIMEOUT = 60000;
	
	private static final int MAX_INBOX_LIFE = 3600;
	
	private static final String SIGNATURE_PREFIX = "-- ";
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ProjectManager projectManager;
	
	private final UserAuthorizationManager authorizationManager;
	
	private final IssueManager issueManager;
	
	private final IssueCommentManager issueCommentManager;
	
	private final IssueWatchManager issueWatchManager;
	
	private final IssueAuthorizationManager issueAuthorizationManager;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestCommentManager pullRequestCommentManager;
	
	private final PullRequestWatchManager pullRequestWatchManager;
	
	private final ExecutorService executorService;
	
	private final UserManager userManager;
	
	private final EmailAddressManager emailAddressManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Thread thread;
	
	@Inject
	public DefaultMailManager(TransactionManager transactionManager, SettingManager settingManager, 
							  UserManager userManager, ProjectManager projectManager, 
							  UserAuthorizationManager authorizationManager, IssueManager issueManager, 
							  IssueCommentManager issueCommentManager, IssueWatchManager issueWatchManager, 
							  PullRequestManager pullRequestManager, PullRequestCommentManager pullRequestCommentManager, 
							  PullRequestWatchManager pullRequestWatchManager, ExecutorService executorService, 
							  EmailAddressManager emailAddressManager, IssueAuthorizationManager issueAuthorizationManager, 
							  ClusterManager clusterManager) {
		this.transactionManager = transactionManager;
		this.settingManager = settingManager;
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
		this.emailAddressManager = emailAddressManager;
		this.issueAuthorizationManager = issueAuthorizationManager;
		this.clusterManager = clusterManager;
	}
	
	private String getQuoteMark() {
		return "[" + settingManager.getBrandingSetting().getName() + "]";
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(MailManager.class);
	}
	
	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
							  String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
							  @Nullable String senderName, @Nullable String references) {
		transactionManager.runAfterCommit(() -> executorService.execute(() -> {
			try {
				sendMail(toList, ccList, bccList, subject, htmlBody, textBody, replyAddress, 
						senderName, references);
			} catch (Exception e) {
				logger.error("Error sending email (to: " + toList + ", subject: " + subject + ")", e);
			}		
		}));
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
	
    private String createFoldedHeaderValue(String name, String value) {
    	try {
			return MimeUtility.fold(name.length() + 2, MimeUtility.encodeText(value, StandardCharsets.UTF_8.name(), null));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
    }

    private InternetAddress createInetAddress(String emailAddress, @Nullable String name) {
        InternetAddress inetAddress;
		try {
			inetAddress = new InternetAddress(emailAddress);
			inetAddress.setPersonal(name);
			inetAddress.validate();
		} catch (AddressException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
        return inetAddress;
    }
    
	@Override
	public void sendMail(SmtpSetting smtpSetting, Collection<String> toList, Collection<String> ccList,
						 Collection<String> bccList, String subject, String htmlBody, String textBody,
						 @Nullable String replyAddress, @Nullable String senderName, String senderAddress,
						 @Nullable String references) {
		if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty())
			return;

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", smtpSetting.getSmtpHost());
		
		smtpSetting.getSslSetting().configure(properties);
 
		properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(SOCKET_CONNECT_TIMEOUT));
		properties.setProperty("mail.smtp.timeout", String.valueOf(smtpSetting.getTimeout()*1000));
		
		Authenticator authenticator;
		if (smtpSetting.getSmtpUser() != null) {
			properties.setProperty("mail.smtp.auth", "true");
			if (smtpSetting.getSmtpCredential() instanceof OAuthAccessToken)
				properties.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");
			String smtpUser = smtpSetting.getSmtpUser();
			String credentialValue = smtpSetting.getSmtpCredential()!=null? smtpSetting.getSmtpCredential().getValue():null;
			authenticator = new Authenticator() {
				
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(smtpUser, credentialValue);
				}
				
			};	        	
		} else {
			authenticator = null;
		}
		
		try {
			Session session = Session.getInstance(properties, authenticator);	        
			
			MimeMultipart bodyPart = new MimeMultipart("alternative");
			
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(htmlBody, "text/html; charset=" + StandardCharsets.UTF_8.name());
			bodyPart.addBodyPart(htmlPart, 0);
			
			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setText(textBody, StandardCharsets.UTF_8.name());
			bodyPart.addBodyPart(textPart, 0);

			Message message = new MimeMessage(session);
			
			if (references != null) {
				String firstReference = StringUtils.substringBefore(references, " ");
				Map<String, String> headers = CollectionUtils.newHashMap(
						"References", references, 
						"In-Reply-To", firstReference, 
						"Thread-Index", getThreadIndex(firstReference));
				for (Map.Entry<String, String> entry: headers.entrySet()) {
					message.addHeader(entry.getKey(), createFoldedHeaderValue(entry.getKey(), entry.getValue()));
				}
			}
			
			if (senderName == null || senderName.equalsIgnoreCase(BrandingSetting.DEFAULT_NAME)) 
				senderName = getQuoteMark();
			else 
				senderName += " " + getQuoteMark();
			message.setFrom(createInetAddress(senderAddress, senderName));
			
			if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty())
				throw new ExplicitException("At least one receiver address should be specified");
			
			message.setRecipients(RecipientType.TO, 
					toList.stream().map(it->createInetAddress(it, null)).toArray(InternetAddress[]::new));
			message.setRecipients(RecipientType.CC, 
					ccList.stream().map(it->createInetAddress(it, null)).toArray(InternetAddress[]::new));
			message.setRecipients(RecipientType.BCC, 
					bccList.stream().map(it->createInetAddress(it, null)).toArray(InternetAddress[]::new));
			if (replyAddress != null)
				message.setReplyTo(new InternetAddress[]{createInetAddress(replyAddress, null)});

			message.setSubject(subject);
			message.setContent(bodyPart);

			logger.debug("Sending email (subject: {}, to: {}, cc: {}, bcc: {})... ", subject, toList, ccList, bccList);
			
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendMail(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
						 String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
						 @Nullable String senderName, @Nullable String references) {
		var mailService = settingManager.getMailService();
		if (mailService != null) {
			mailService.sendMail(toList, ccList, bccList, subject, htmlBody, textBody, replyAddress,
					senderName, references);
		} else {
			logger.warn("Unable to send mail as mail service is not configured");
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == MAIL_SERVICE) {
				transactionManager.runAfterCommit(() -> clusterManager.submitToServer(clusterManager.getLeaderServerAddress(), () -> {
					Thread copy = thread;
					if (copy != null)
						copy.interrupt();
					return null;
				}));
			}
		}
	}

	private void checkPermission(InternetAddress sender, Project project, BasePermission privilege, 
			@Nullable User user, @Nullable SenderAuthorization authorization) {
		if ((user == null || !user.asSubject().isPermitted(new ProjectPermission(project, privilege))) 
				&& (authorization == null || !authorization.isPermitted(project, privilege))) {
			String errorMessage = String.format("Permission denied (project: %s, sender: %s, permission: %s)", 
					project.getPath(), sender.getAddress(), privilege.getClass().getName());
			throw new UnauthorizedException(errorMessage);
		}
	}
	
	private String getThreadingReferences(String uuid, @Nullable String messageId) {
		var threadingReferences = "<" + uuid + "@onedev>";
		if (messageId != null)
			threadingReferences = messageId + " " + threadingReferences;
		return threadingReferences;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void handleMessage(Message message, String systemAddress, List<String> additionalTargetAddresses) {
		var fromInternetAddresses = getAddresses(message, "From");
		var toInternetAddresses = getAddresses(message, "To");
		if (fromInternetAddresses.length != 0 && toInternetAddresses.length != 0) {
			Set<String> targetAddresses = Sets.newHashSet(systemAddress.toLowerCase());
			for (var additionalTargetAddress: additionalTargetAddresses) 
				targetAddresses.add(additionalTargetAddress.toLowerCase());
			
			var fromInternetAddress = fromInternetAddresses[0];
			var fromAddress = fromInternetAddress.getAddress();
			if (!fromAddress.equalsIgnoreCase(systemAddress)) {
				EmailAddress fromAddressEntity = emailAddressManager.findByValue(fromAddress);
				if (fromAddressEntity != null && !fromAddressEntity.isVerified()) {
					logger.error("Another account uses email address '{}' but not verified", fromAddress);
				} else {
					User fromUser = fromAddressEntity != null ? fromAddressEntity.getOwner() : null;
					SenderAuthorization authorization = null;
					String designatedProject = null;
					ServiceDeskSetting serviceDeskSetting = settingManager.getServiceDeskSetting();
					if (serviceDeskSetting != null) {
						authorization = serviceDeskSetting.getSenderAuthorization(fromAddress);
						designatedProject = serviceDeskSetting.getDesignatedProject(fromAddress);
					}
					
					var parsedSystemAddress = ParsedEmailAddress.parse(systemAddress);
					
					Issue involvedIssue = null;
					PullRequest involvedPullRequest = null;
					Collection<InternetAddress> involvedInternetAddresses = new HashSet<>();

					List<InternetAddress> receiverInternetAddresses = new ArrayList<>();
					receiverInternetAddresses.addAll(asList(toInternetAddresses));
					receiverInternetAddresses.addAll(asList(getAddresses(message, "Cc")));
					for (InternetAddress receiverInternetAddress : receiverInternetAddresses) {
						ParsedEmailAddress parsedReceiverAddress = ParsedEmailAddress.parse(receiverInternetAddress.getAddress());
						if (targetAddresses.contains(parsedReceiverAddress.toString().toLowerCase())) {
							if (involvedIssue == null && involvedPullRequest == null) {
								if (isOriginatedFromOneDev(message)) {
									logger.warn("Ignored opening issue from message as it is originated from OneDev");
								} else if (serviceDeskSetting != null) {
									if (designatedProject == null)
										throw new ExplicitException("No project designated for sender: " + fromInternetAddress.getAddress());
									Project project = projectManager.findByPath(designatedProject);
									if (project == null) {
										String errorMessage = String.format(
												"Sender project does not exist (sender: %s, project: %s)",
												fromInternetAddress.getAddress(), designatedProject);
										throw new ExplicitException(errorMessage);
									}
									checkPermission(fromInternetAddress, project, new AccessProject(), fromUser, authorization);
									involvedIssue = openIssue(message, project, fromInternetAddress, fromUser, authorization, parsedSystemAddress);
								} else {
									throw new ExplicitException("Unable to create issue from email as service desk is not enabled");
								}
							} else {
								logger.warn("Ignored recipient '" + parsedReceiverAddress + "' as issue or pull request is processed");
							}
						} else if (parsedReceiverAddress.isSubaddress() && targetAddresses.contains(parsedReceiverAddress.getOriginalAddress().toLowerCase())) {
							if (involvedIssue == null && involvedPullRequest == null) {
								String subAddress = StringUtils.substringAfter(parsedReceiverAddress.getName(), "+");
								if (subAddress.equals(MailManager.TEST_SUB_ADDRESS)) {
									break;
								} else if (subAddress.contains("~")) {
									Long entityId;
									try {
										entityId = Long.parseLong(StringUtils.substringAfter(subAddress, "~"));
									} catch (NumberFormatException e) {
										throw new ExplicitException("Invalid id specified in recipient address: " + parsedReceiverAddress);
									}
									if (subAddress.contains("issue")) {
										involvedIssue = issueManager.get(entityId);
										if (involvedIssue == null)
											throw new ExplicitException("Non-existent issue specified in receipient address: " + parsedReceiverAddress);
										if (subAddress.contains("unsubscribe")) {
											if (fromUser != null) {
												IssueWatch watch = issueWatchManager.find(involvedIssue, fromUser);
												if (watch != null) {
													watch.setWatching(false);
													issueWatchManager.createOrUpdate(watch);
													String subject = "Unsubscribed successfully from issue " + involvedIssue.getReference();
													String template = settingManager.getEmailTemplates().getIssueNotificationUnsubscribed();

													Map<String, Object> bindings = new HashMap<>();
													bindings.put("issue", involvedIssue);

													String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
													String textBody = EmailTemplates.evalTemplate(false, template, bindings);

													var threadingReferences = getThreadingReferences(UUID.randomUUID().toString(), getMessageId(message));
													sendMailAsync(newArrayList(fromInternetAddress.getAddress()), newArrayList(), newArrayList(),
															subject, htmlBody, textBody, null, null, threadingReferences);
												}
											}
										} else {
											checkPermission(fromInternetAddress, involvedIssue.getProject(), new AccessProject(), fromUser, authorization);
											addComment(involvedIssue, message, fromInternetAddress, fromUser, authorization, receiverInternetAddresses);
										}
									} else if (subAddress.contains("pullrequest")) {
										involvedPullRequest = pullRequestManager.get(entityId);
										if (involvedPullRequest == null)
											throw new ExplicitException("Non-existent pull request specified in receipient address: " + parsedReceiverAddress);
										if (subAddress.contains("unsubscribe")) {
											if (fromUser != null) {
												PullRequestWatch watch = pullRequestWatchManager.find(involvedPullRequest, fromUser);
												if (watch != null) {
													watch.setWatching(false);
													pullRequestWatchManager.createOrUpdate(watch);
													String subject = "Unsubscribed successfully from pull request " + involvedPullRequest.getReference().toString(null);

													String template = StringUtils.join(settingManager.getEmailTemplates().getPullRequestNotificationUnsubscribed(), "\n");
													Map<String, Object> bindings = new HashMap<>();
													bindings.put("pullRequest", involvedPullRequest);
													String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
													String textBody = EmailTemplates.evalTemplate(false, template, bindings);
													var threadingReferences = getThreadingReferences(UUID.randomUUID().toString(), getMessageId(message));
													sendMailAsync(newArrayList(fromInternetAddress.getAddress()), newArrayList(), newArrayList(),
															subject, htmlBody, textBody, null, null, threadingReferences);
												}
											}
										} else {
											checkPermission(fromInternetAddress, involvedPullRequest.getProject(), new ReadCode(), fromUser, authorization);
											addComment(involvedPullRequest, message, fromInternetAddress, fromUser, authorization, receiverInternetAddresses);
										}
									} else {
										throw new ExplicitException("Invalid recipient address: " + parsedReceiverAddress);
									}
								} else {
									if (isOriginatedFromOneDev(message)) {
										logger.warn("Ignored opening issue from message as it is originated from OneDev");
									} else {
										Project project = projectManager.findByServiceDeskName(subAddress);
										if (project == null)
											project = projectManager.findByPath(subAddress);

										if (project == null)
											throw new ExplicitException("Non-existent project specified in receipient address: " + parsedReceiverAddress);
										if (serviceDeskSetting != null) {
											checkPermission(fromInternetAddress, project, new AccessProject(), fromUser, authorization);
											logger.debug("Creating issue via email (project: {})...", project.getPath());
											involvedIssue = openIssue(message, project, fromInternetAddress, fromUser, authorization, parsedSystemAddress);
										} else {
											throw new ExplicitException("Unable to create issue from email as service desk is not enabled");
										}
									}
								}
							} else {
								logger.warn("Ignored recipient '" + parsedReceiverAddress + "' as issue or pull request is processed");
							}
						} else if (!receiverInternetAddress.equals(fromInternetAddress)) {
							involvedInternetAddresses.add(receiverInternetAddress);
						}
					}

					if (involvedIssue != null) {
						var project = involvedIssue.getProject();
						for (InternetAddress involvedInternetAddress : involvedInternetAddresses) {
							EmailAddress involvedAddressEntity = emailAddressManager.findByValue(involvedInternetAddress.getAddress());
							if (involvedAddressEntity != null && !involvedAddressEntity.isVerified()) {
								logger.error("Another account uses email address '{}' but not verified", involvedInternetAddress.getAddress());
							} else {
								if (serviceDeskSetting != null)
									authorization = serviceDeskSetting.getSenderAuthorization(involvedInternetAddress.getAddress());
								var involvedUser = involvedAddressEntity != null ? involvedAddressEntity.getOwner() : null;
								try {
									checkPermission(involvedInternetAddress, project, new AccessProject(), involvedUser, authorization);
									if (involvedUser == null)
										involvedUser = createUser(involvedInternetAddress, project, authorization.getAuthorizedRole());
									else if (!involvedUser.asSubject().isPermitted(new ProjectPermission(project, new AccessProject())))
										authorizationManager.authorize(involvedUser, project, authorization.getAuthorizedRole());
									issueWatchManager.watch(involvedIssue, involvedUser, true);
									if (involvedIssue.isConfidential())
										issueAuthorizationManager.authorize(involvedIssue, involvedUser);
								} catch (UnauthorizedException e) {
									logger.error("Error adding receipient to watch list", e);
								}
							}
						}
					} else if (involvedPullRequest != null) {
						var project = involvedPullRequest.getProject();
						for (InternetAddress involvedInternetAddress : involvedInternetAddresses) {
							EmailAddress involvedAddressEntity = emailAddressManager.findByValue(involvedInternetAddress.getAddress());
							if (involvedAddressEntity != null && !involvedAddressEntity.isVerified()) {
								logger.error("Another account uses email address '{}' but not verified", involvedInternetAddress.getAddress());
							} else {
								var involvedUser = involvedAddressEntity != null ? involvedAddressEntity.getOwner() : null;
								if (serviceDeskSetting != null)
									authorization = serviceDeskSetting.getSenderAuthorization(involvedInternetAddress.getAddress());
								try {
									checkPermission(involvedInternetAddress, involvedPullRequest.getProject(), new ReadCode(), involvedUser, authorization);
									if (involvedUser == null)
										involvedUser = createUser(involvedInternetAddress, involvedPullRequest.getProject(), authorization.getAuthorizedRole());
									else if (!involvedUser.asSubject().isPermitted(new ProjectPermission(project, new ReadCode())))
										authorizationManager.authorize(involvedUser, project, authorization.getAuthorizedRole());
									pullRequestWatchManager.watch(involvedPullRequest, involvedUser, true);
								} catch (UnauthorizedException e) {
									logger.error("Error adding receipient to watch list", e);
								}
							}
						}
					}
				}
			} else {
				logger.warn("Ignore message as 'From' is same as system email address");
			}
		} else {
			logger.warn("Ignore message as 'To' or 'From' header is not available");
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

	private String stripTextSignature(String content) {
		var lines = new ArrayList<>();
		for (var line: Splitter.on('\n').split(content)) {
			if (line.contains(SIGNATURE_PREFIX)) {
				Document document = HtmlUtils.parse(line);				
				if (document.wholeText().trim().equals(SIGNATURE_PREFIX.trim()))
					break;
				else 
					lines.add(line);
			} else {
				lines.add(line);
			}
		}
		return StringUtils.join(lines, "\n");
	}
	
	@Nullable
	private String stripSignature(String content) {
		Document document = HtmlUtils.parse(stripTextSignature(content));
		document.outputSettings().prettyPrint(false);
		return getContent(document);
	}	
	
	@Nullable
	private String getContent(Document document) {
		AtomicReference<Node> lastContentNodeRef = new AtomicReference<>(null);
		
		NodeTraversor.traverse(new NodeVisitor() {
			
			@Override
			public void tail(Node node, int depth) {
				if (node instanceof Element && ((Element) node).tagName().equals("img")) {
					lastContentNodeRef.set(node);
				} else if (node instanceof TextNode) {
					String text = ((TextNode) node).getWholeText();
					char nbsp = 160;
					if (StringUtils.isNotBlank(StringUtils.replaceChars(text, nbsp, ' '))) 
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
	
	private String decorateContent(String content) {
		// Add double line breaks in the beginning and ending as otherwise plain text content 
		// with multiple paragraphs received from email may not be formatted correctly with 
		// our markdown renderer. 
		return String.format("<div class='%s'>\n\n", COMMENT_MARKER) + content + "\n\n</div>";
	}
	
	private void addComment(Issue issue, Message message, InternetAddress authorInternetAddress, @Nullable User author, 
							@Nullable SenderAuthorization authorization, Collection<InternetAddress> notifiedInternetAddresses) {
		IssueComment comment = new IssueComment();
		comment.setIssue(issue);
		if (author == null)
			author = createUser(authorInternetAddress, issue.getProject(), authorization.getAuthorizedRole());
		else if (!author.asSubject().isPermitted(new ProjectPermission(issue.getProject(), new AccessProject())))
			authorizationManager.authorize(author, issue.getProject(), authorization.getAuthorizedRole());
		comment.setUser(author);
		String content = parseBody(message, issue.getProject(), issue.getUUID());
		if (content != null) {
			// Add double line breaks in the beginning and ending as otherwise plain text content 
			// received from email may not be formatted correctly with our markdown renderer. 
			comment.setContent(decorateContent(content));
			issueCommentManager.create(comment, notifiedInternetAddresses.stream().map(InternetAddress::getAddress).collect(toList()));
		}
	}
	
	private void addComment(PullRequest pullRequest, Message message, InternetAddress authorInternetAddress, @Nullable User author,
							@Nullable SenderAuthorization authorization, Collection<InternetAddress> notifiedInternetAddresses) {
		PullRequestComment comment = new PullRequestComment();
		comment.setRequest(pullRequest);
		if (author == null)
			author = createUser(authorInternetAddress, pullRequest.getProject(), authorization.getAuthorizedRole());
		else if (!author.asSubject().isPermitted(new ProjectPermission(pullRequest.getProject(), new ReadCode())))
			authorizationManager.authorize(author, pullRequest.getProject(), authorization.getAuthorizedRole());
		comment.setUser(author);
		String content = parseBody(message, pullRequest.getProject(), pullRequest.getUUID());
		if (content != null) {
			comment.setContent(decorateContent(content));
			pullRequestCommentManager.create(comment, notifiedInternetAddresses.stream().map(InternetAddress::getAddress).collect(toList()));
		}
	}
	
	private Issue openIssue(Message message, Project project, InternetAddress submitterInternetAddress, 
			@Nullable User submitter, @Nullable SenderAuthorization authorization, 
			ParsedEmailAddress parsedSystemAddress) {
		Issue issue = new Issue();
		issue.setProject(project);

		try {
			var subject = message.getSubject();
			if (StringUtils.isBlank(subject)) {
				throw new ExplicitException("Subject required to open issue via email");
			} else if (subject.trim().toLowerCase().startsWith("re:")) {
				throw new ExplicitException("This address is intended to open issues, " +
						"however the message looks like a reply to some other email");
			}
			issue.setTitle(subject);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		
		String messageId = getMessageId(message);
		if (messageId != null)
			issue.setMessageId(messageId);

		String description = parseBody(message, project, issue.getUUID());
		if (StringUtils.isNotBlank(description)) 
			description = stripSignature(description);
		if (StringUtils.isNotBlank(description))
			issue.setDescription(decorateContent(description));

		if (submitter == null) 
			submitter = createUser(submitterInternetAddress, project, authorization.getAuthorizedRole());
		else if (!submitter.asSubject().isPermitted(new ProjectPermission(project, new AccessProject()))) 
			authorizationManager.authorize(submitter, project, authorization.getAuthorizedRole());
		issue.setSubmitter(submitter);
		
		GlobalIssueSetting issueSetting = settingManager.getIssueSetting();
		issue.setState(issueSetting.getInitialStateSpec().getName());
		
		IssueCreationSetting issueCreationSetting = settingManager.getServiceDeskSetting()
				.getIssueCreationSetting(submitterInternetAddress.getAddress(), project);
		issue.setConfidential(issueCreationSetting.isConfidential());
		for (FieldInstance instance: issueCreationSetting.getIssueFields()) {
			Object fieldValue = issueSetting.getFieldSpec(instance.getName())
					.convertToObject(instance.getValueProvider().getValue());
			issue.setFieldValue(instance.getName(), fieldValue);
		}
		
		issueManager.open(issue);
		
		ParsedEmailAddress parsedSubmitterAddress = ParsedEmailAddress.parse(submitterInternetAddress.getAddress());
		if (!parsedSubmitterAddress.getDomain().equalsIgnoreCase(parsedSystemAddress.getDomain()) 
				|| !parsedSubmitterAddress.getName().toLowerCase().startsWith(parsedSystemAddress.getName().toLowerCase() + "+") 
						&& !parsedSubmitterAddress.getName().equalsIgnoreCase(parsedSystemAddress.getName())) {
			
			String template = StringUtils.join(settingManager.getEmailTemplates().getServiceDeskIssueOpened(), "\n");
			Map<String, Object> bindings = new HashMap<>();
			bindings.put("issue", issue);
			String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
			String textBody = EmailTemplates.evalTemplate(false, template, bindings);
			
			var replyAddress = parsedSystemAddress.getSubaddress("issue~" + issue.getId());
			sendMailAsync(newArrayList(submitterInternetAddress.getAddress()), newArrayList(), newArrayList(),
					"Re: " + issue.getTitle(), htmlBody, textBody, replyAddress, 
					submitterInternetAddress.getPersonal(), issue.getThreadingReferences()); 
		}
		return issue;
	}

	private User createUser(InternetAddress internetAddress, Project project, Role role) {
		User user = new User();
		user.setName(UserNameValidator.suggestUserName(ParsedEmailAddress.parse(internetAddress.getAddress()).getName()));
		user.setFullName(internetAddress.getPersonal());
		user.setPassword("impossible password");
		user.setGuest(true);
		userManager.create(user);
		
		EmailAddress addressEntity = new EmailAddress();
		addressEntity.setValue(internetAddress.getAddress());
		addressEntity.setVerificationCode(null);
		addressEntity.setPrimary(true);
		addressEntity.setGit(true);
		addressEntity.setOwner(user);
		emailAddressManager.create(addressEntity);
		
		authorizationManager.authorize(user, project, role);
		
		return user;
	}
	
	@Listen
	public void on(SystemStarted event) {
		clusterManager.getHazelcastInstance().getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterManager.isLeaderServer()) {
					Thread copy = thread;
					if (copy != null)
						copy.interrupt();
				}
			}
			
		});
		thread = new Thread(() -> {
			while (thread != null) {
				try {
					var mailService = settingManager.getMailService();
					var inboxMonitor = mailService != null? mailService.getInboxMonitor(): null;
					if (inboxMonitor != null && clusterManager.isLeaderServer()) {
						var systemAddress = mailService.getSystemAddress();
						while (thread != null) {
							Future<?> future = inboxMonitor.monitor(message -> {
								handleMessage(message, systemAddress, inboxMonitor.getAdditionalTargetAddresses());
							}, false);
							try {
								future.get();
							} catch (InterruptedException e) {
								future.cancel(true);
								throw e;
							} catch (ExecutionException e) {
								if (ExceptionUtils.find(e, FolderClosedException.class) == null)
									logger.error("Error monitoring inbox", e);
								else
									logger.warn("Lost connection to mail server, will reconnect later... ");
								Thread.sleep(5000);
							}
						}
					} else {
						Thread.sleep(60000);
					}
				} catch (InterruptedException ignored) {
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
			} catch (InterruptedException ignored) {
			}
		}
	}
	
	@Override
	public Future<?> monitorInbox(ImapSetting imapSetting, String systemAddress, 
								  Consumer<Message> messageConsumer,
								  MailPosition lastPosition, boolean testMode) {
		return executorService.submit(new Runnable() {

			private void processMessages(IMAPFolder inbox, AtomicInteger messageNumber) throws MessagingException {
				int messageCount = inbox.getMessageCount();
				for (int i=messageNumber.get()+1; i<=messageCount; i++) {
					Message message = inbox.getMessage(i);
					lastPosition.setUid(inbox.getUID(message));
					logger.trace("Processing inbox message (subject: {}, uid: {}, seq: {})", 
							message.getSubject(), lastPosition.getUid(), i);
					try {
						messageConsumer.accept(message);
					} catch (Exception e) {
						try {
							String[] fromHeader = message.getHeader("From");
							if (fromHeader != null && fromHeader.length != 0 
									&& !fromHeader[0].equalsIgnoreCase(systemAddress)) {
								InternetAddress from = InternetAddress.parse(fromHeader[0], true)[0];
								
								String template = StringUtils.join(settingManager.getEmailTemplates().getServiceDeskIssueOpenFailed(), "\n");
								Map<String, Object> bindings = new HashMap<>();
								bindings.put("exception", e);
								
								String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
								String textBody = EmailTemplates.evalTemplate(false, template, bindings);
								
								var threadingReferences = getThreadingReferences(UUID.randomUUID().toString(), null);
								sendMailAsync(newArrayList(from.getAddress()), new ArrayList<>(), 
										new ArrayList<>(), "OneDev is unable to process your message", 
										htmlBody, textBody, null, null, threadingReferences);								
							}
						} catch (Exception e2) {
							logger.error("Error sending mail", e);
						}
						logger.error("Error processing message", e);
					} 
				}
				messageNumber.set(messageCount);
			}
			
			private long getUid(IMAPFolder inbox, int messageNumber) throws MessagingException {
				if (messageNumber != 0)
					return inbox.getUID(inbox.getMessage(messageNumber));
				else
					return -1;
			}
			
			@Override
			public void run() {
		        Properties properties = new Properties();
		        
		        properties.setProperty("mail.imap.host", imapSetting.getImapHost());
				imapSetting.getSslSetting().configure(properties);
		        properties.setProperty("mail.imap.connectiontimeout", String.valueOf(SOCKET_CONNECT_TIMEOUT));
		        properties.setProperty("mail.imap.timeout", String.valueOf(imapSetting.getTimeout()*1000));
	        	if (imapSetting.getImapCredential() instanceof OAuthAccessToken)
	        		properties.setProperty("mail.imap.auth.mechanisms", "XOAUTH2");
				
				Session session = Session.getInstance(properties);
				Store store = null;
				IMAPFolder inbox = null;
				try {
					store = session.getStore("imap");
					String credentialValue = imapSetting.getImapCredential().getValue();
					store.connect(imapSetting.getImapUser(), credentialValue);
					inbox = (IMAPFolder) store.getFolder("INBOX");
					inbox.open(Folder.READ_ONLY);
					
					long uidValidity = inbox.getUIDValidity();
					AtomicInteger messageNumber = new AtomicInteger(0);
					if (uidValidity == lastPosition.getUidValidity()) {
						logger.trace("Inbox uid validity unchanged (uid: {})", lastPosition.getUid());
						if (lastPosition.getUid() != -1) {
							Message lastMessage = inbox.getMessageByUID(lastPosition.getUid());
							if (lastMessage != null) {
								logger.trace("Last processed inbox message found (subject: {}, uid: {}, seq: {})", 
										lastMessage.getSubject(), lastPosition.getUid(), lastMessage.getMessageNumber());
								messageNumber.set(lastMessage.getMessageNumber());
								processMessages(inbox, messageNumber);
							} else {
								messageNumber.set(inbox.getMessageCount());
								lastPosition.setUid(getUid(inbox, messageNumber.get()));
								logger.trace("Last processed inbox message not found (uid reset to: {})", lastPosition.getUid());
							}
						} else {
							processMessages(inbox, messageNumber);
						}
					} else {
						lastPosition.setUidValidity(uidValidity);
						if (testMode)
							messageNumber.set(inbox.getMessageCount() - 5);
						else
							messageNumber.set(inbox.getMessageCount());							
						if (messageNumber.get() < 0)
							messageNumber.set(0);
						lastPosition.setUid(getUid(inbox, messageNumber.get()));
						logger.trace("Inbox uid validity changed (uid reset to: {})", lastPosition.getUid());
					}

					long time = System.currentTimeMillis();
					while (true) { 
						if (testMode)
							Thread.sleep(5000);
						else
							Thread.sleep(imapSetting.getPollInterval()*1000);
						processMessages(inbox, messageNumber);
						
						// discard inbox periodically to save memory
						if (System.currentTimeMillis()-time > MAX_INBOX_LIFE*1000)
							break;
					}
					
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				} finally {
					if (inbox != null && inbox.isOpen()) {
						try {
							inbox.close(false);
						} catch (Exception ignored) {
						}
					}
					if (store != null) {
						try {
							store.close();
						} catch (Exception ignored) {
						}
					}
				}
			}
		});
	}

	private String getFeedbackAddress(String subAddress) {
		var mailService = settingManager.getMailService();
		if (mailService != null && mailService.getInboxMonitor() != null) 
			return ParsedEmailAddress.parse(mailService.getSystemAddress()).getSubaddress(subAddress);
		else 
			return null;
	}

	@Override
	public String getReplyAddress(Issue issue) {
		return getFeedbackAddress("issue~" + issue.getId());
	}
	
	@Override
	public String getReplyAddress(PullRequest request) {
		return getFeedbackAddress("pullrequest~" + request.getId());
	}
	
	@Override
	public String getUnsubscribeAddress(Issue issue) {
		return getFeedbackAddress("issueunsubscribe~" + issue.getId());
	}

	@Override
	public String getUnsubscribeAddress(PullRequest request) {
		return getFeedbackAddress("pullrequestunsubscribe~" + request.getId());
	}
	
	@Override
	public String toPlainText(String mailContent) {
		OutputSettings outputSettings = new OutputSettings();
		outputSettings.prettyPrint(false);
		String plainText = Jsoup.clean(mailContent, "", Safelist.none(), outputSettings);
		plainText = Joiner.on('\n').join(Splitter.on('\n').trimResults().split(plainText));
		plainText = plainText.replaceAll("\n\n(\n)+", "\n\n").trim();
		return plainText;
	}

	@Override
	public boolean isMailContent(String comment) {
		return comment.contains(String.format("<div class='%s'>", COMMENT_MARKER));
	}

	@Nullable
	private String getMessageId(Message message) {
		try {
			var messageId = message.getHeader("Message-ID");
			if (messageId != null && messageId.length != 0)
				return messageId[0];
			else
				return null;
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private InternetAddress[] getAddresses(Message message, String header) {
		try {
			String[] values = message.getHeader(header);
			if (values != null && values.length != 0)
				return InternetAddress.parse(values[0], true);
			else 
				return new InternetAddress[0];
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isOriginatedFromOneDev(Message message) {
		try {
			var references = message.getHeader("References");
			return references != null && references.length != 0 && references[0].contains("@onedev>");
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	private String parseBody(Message message, Project project, String attachmentGroup) {
		try {
			Attachments attachments = new Attachments();
			fillAttachments(project, attachmentGroup, message, attachments);
			String body = parseBody(project, attachmentGroup, message, attachments);
			
			String quotationMark = null;
			if (body.contains(getQuoteMark()))
				quotationMark = getQuoteMark();

			if (quotationMark != null) {
				body = StringUtils.substringBefore(body, quotationMark);
				body = StringUtils.substringBeforeLast(body, "\n");
			}
			body = stripTextSignature(body);

			attachments.identifiable.keySet().removeAll(attachments.referenced);
			attachments.nonIdentifiable.addAll(attachments.identifiable.values());
			if (!attachments.nonIdentifiable.isEmpty()) {
				body += "\n\n---";
				List<String> markdowns = new ArrayList<>();
				for (Attachment attachment: attachments.nonIdentifiable)
					markdowns.add(attachment.getMarkdown());
				body += "\n\n" + Joiner.on(" &nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp; ").join(markdowns);
			}
			
			var document = HtmlUtils.parse(body);
			document.outputSettings().prettyPrint(false);

			return getContent(document);
		} catch (IOException | MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private String parseBody(Project project, String attachmentGroup, Part part, Attachments attachments)
			throws IOException, MessagingException {
		if (part.getDisposition() == null) {
			if (part.isMimeType("text/plain")) {
				return HtmlEscape.escapeHtml5(part.getContent().toString());
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
						return parseBody(project, attachmentGroup, multipart.getBodyPart(count - 1), attachments);
					StringBuilder builder = new StringBuilder();
					for (int i=0; i<count; i++)
						builder.append(parseBody(project, attachmentGroup, multipart.getBodyPart(i), attachments));
					return builder.toString();
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
			var attachmentManager = OneDev.getInstance(AttachmentManager.class);
			String attachmentName = attachmentManager.saveAttachment(project.getId(), attachmentGroup,
					fileName, part.getInputStream());
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

}

package io.onedev.server.mail;

import static com.google.common.collect.Lists.newArrayList;
import static io.onedev.server.model.Setting.Key.MAIL;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

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

import com.google.common.base.Joiner;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.ibm.icu.impl.locale.XCldrStub.Splitter;
import com.sun.mail.imap.IMAPFolder;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.EmailAddressService;
import io.onedev.server.service.IssueAuthorizationService;
import io.onedev.server.service.IssueCommentService;
import io.onedev.server.service.IssueService;
import io.onedev.server.service.IssueWatchService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestCommentService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.service.PullRequestWatchService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.Setting;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.administration.IssueCreationSetting;
import io.onedev.server.model.support.administration.emailtemplates.EmailTemplates;
import io.onedev.server.model.support.issue.field.instance.FieldInstance;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.util.ParsedEmailAddress;

@Singleton
public class DefaultMailService implements MailService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMailService.class);

	private static final int SOCKET_CONNECT_TIMEOUT = 60000;
	
	private static final int MAX_INBOX_LIFE = 3600;
	
	private static final String SIGNATURE_PREFIX = "-- ";

	@Inject
	private SettingService settingService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private ProjectService projectService;

	@Inject
	private IssueService issueService;

	@Inject
	private IssueCommentService issueCommentService;

	@Inject
	private IssueWatchService issueWatchService;

	@Inject
	private IssueAuthorizationService issueAuthorizationService;

	@Inject
	private PullRequestService pullRequestService;

	@Inject
	private PullRequestCommentService pullRequestCommentService;

	@Inject
	private PullRequestWatchService pullRequestWatchService;

	@Inject
	private ExecutorService executorService;

	@Inject
	private UserService userService;

	@Inject
	private EmailAddressService emailAddressService;

	@Inject
	private ClusterService clusterService;

	private volatile Boolean prodTest;
	
	private volatile Thread thread;

	private boolean isProdTest() {
		if (prodTest == null) {
			prodTest = settingService.getSystemSetting().getServerUrl().equals("https://code.onedev.io") 
					&& !new File("/home/onedev/website").exists();
		}
		return prodTest;
	}
	
	private String getQuoteMark() {
		return "[" + settingService.getBrandingSetting().getName() + "]";
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(MailService.class);
	}
	
	@Sessional
	@Override
	public void sendMailAsync(Collection<String> toList, Collection<String> ccList, Collection<String> bccList, 
							  String subject, String htmlBody, String textBody, @Nullable String replyAddress, 
							  @Nullable String senderName, @Nullable String references) {
		transactionService.runAfterCommit(() -> executorService.execute(() -> {
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
	
	@Nullable
	private Project findProject(String systemAddress, ParsedEmailAddress receiverEmailAddress) {
		var project = projectService.findByServiceDeskEmailAddress(receiverEmailAddress.toString());
		if (project == null 
				&& receiverEmailAddress.isSubaddress() 
				&& receiverEmailAddress.getOriginalAddress().equalsIgnoreCase(systemAddress)) {
			String subAddress = StringUtils.substringAfter(receiverEmailAddress.getName(), "+");
			project = projectService.findByPath(subAddress);			
		}
		return project;
	}
    
	@Override
	public void sendMail(SmtpSetting smtpSetting, Collection<String> toList, Collection<String> ccList,
						 Collection<String> bccList, String subject, String htmlBody, String textBody,
						 @Nullable String replyAddress, @Nullable String senderName, String senderAddress,
						 @Nullable String references) {
		if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty())
			return;

		if (isProdTest()) {
			logger.warn("Ignore sending mail as in prod test mode");
			return;
		}

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
			
			if (senderName == null || senderName.equalsIgnoreCase("onedev")) 
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
		var mailConnector = settingService.getMailConnector();
		if (mailConnector != null) {
			mailConnector.sendMail(toList, ccList, bccList, subject, htmlBody, textBody, replyAddress,
					senderName, references, false);
		} else {
			logger.warn("Unable to send mail as mail service is not configured");
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Setting) {
			Setting setting = (Setting) event.getEntity();
			if (setting.getKey() == MAIL) {
				transactionService.runAfterCommit(() -> clusterService.submitToServer(clusterService.getLeaderServerAddress(), () -> {
					Thread copy = thread;
					if (copy != null)
						copy.interrupt();
					return null;
				}));
			}
		}
	}
	
	private String getThreadingReferences(String uuid, @Nullable String messageId) {
		var threadingReferences = "<" + uuid + "@onedev>";
		if (messageId != null)
			threadingReferences = messageId + " " + threadingReferences;
		return threadingReferences;
	}
	
	@Transactional
	@Override
	public void handleMessage(Message message, String systemAddress) {
		var fromInternetAddresses = getAddresses(message, "From");
		var toInternetAddresses = getAddresses(message, "To");
		if (fromInternetAddresses.length != 0 && toInternetAddresses.length != 0) {
			var fromInternetAddress = fromInternetAddresses[0];
			var fromAddress = fromInternetAddress.getAddress();
			if (!fromAddress.equalsIgnoreCase(systemAddress)) {
				EmailAddress fromAddressEntity = emailAddressService.findByValue(fromAddress);
				User fromUser = null;
				if (fromAddressEntity != null && fromAddressEntity.isVerified())
					fromUser = fromAddressEntity.getOwner();
				
				var parsedSystemAddress = ParsedEmailAddress.parse(systemAddress);
				
				Issue involvedIssue = null;
				PullRequest involvedPullRequest = null;
				Collection<InternetAddress> involvedInternetAddresses = new HashSet<>();

				List<InternetAddress> receiverInternetAddresses = new ArrayList<>();
				receiverInternetAddresses.addAll(asList(toInternetAddresses));
				receiverInternetAddresses.addAll(asList(getAddresses(message, "Cc")));
				for (InternetAddress receiverInternetAddress : receiverInternetAddresses) {
					ParsedEmailAddress parsedReceiverAddress = ParsedEmailAddress.parse(receiverInternetAddress.getAddress());
					var project = findProject(systemAddress, parsedReceiverAddress);
					if (project != null) {
						if (isOriginatedFromOneDev(message)) {
							logger.warn("Ignored opening issue from message as it is originated from OneDev");
						} else {
							var serviceDeskSetting = settingService.getServiceDeskSetting();
							if (serviceDeskSetting != null) {
								logger.debug("Creating issue via email (project: {})...", project.getPath());
								involvedIssue = openIssue(message, project, fromInternetAddress, fromUser, parsedSystemAddress, receiverInternetAddresses);
							} else {
								throw new ExplicitException("Unable to create issue from email as service desk is not enabled");
							}
						}
					} else if (parsedReceiverAddress.isSubaddress() && systemAddress.equalsIgnoreCase(parsedReceiverAddress.getOriginalAddress())) {
						if (involvedIssue == null && involvedPullRequest == null) {
							String subAddress = StringUtils.substringAfter(parsedReceiverAddress.getName(), "+");
							if (subAddress.equals(MailService.TEST_SUB_ADDRESS)) {
								break;
							} else if (subAddress.contains("~")) {
								Long entityId;
								try {
									entityId = Long.parseLong(StringUtils.substringAfter(subAddress, "~"));
								} catch (NumberFormatException e) {
									throw new ExplicitException("Invalid id specified in recipient address: " + parsedReceiverAddress);
								}
								if (subAddress.contains("issue")) {
									involvedIssue = issueService.get(entityId);
									if (involvedIssue == null)
										throw new ExplicitException("Non-existent issue specified in recipient address: " + parsedReceiverAddress);
									if (subAddress.contains("unsubscribe")) {
										if (fromUser != null) {
											var watch = issueWatchService.find(involvedIssue, fromUser);
											if (watch != null) 
												watch.setWatching(false);
										} else {
											involvedIssue.getExternalParticipants().remove(fromInternetAddress);
										}											
										String subject = "Unsubscribed successfully from issue " + involvedIssue.getReference();
										String template = settingService.getEmailTemplates().getIssueNotificationUnsubscribed();

										Map<String, Object> bindings = new HashMap<>();
										bindings.put("issue", involvedIssue);

										String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
										String textBody = EmailTemplates.evalTemplate(false, template, bindings);

										var threadingReferences = getThreadingReferences(UUID.randomUUID().toString(), getMessageId(message));
										sendMailAsync(newArrayList(fromInternetAddress.getAddress()), newArrayList(), newArrayList(),
												subject, htmlBody, textBody, null, null, threadingReferences);
									} else {
										if (fromUser != null) {
											if (SecurityUtils.canAccessIssue(fromUser.asSubject(), involvedIssue))	
												addComment(involvedIssue, message, fromInternetAddress, fromUser, receiverInternetAddresses);
											else 
												throw new UnauthorizedException("No permission to comment issue: " + involvedIssue.getReference());
										} else {
											if (involvedIssue.getExternalParticipants().contains(fromInternetAddress)) 											
												addComment(involvedIssue, message, fromInternetAddress, null, receiverInternetAddresses);
											else
												throw new UnauthorizedException("Not eligible to comment issue: " + involvedIssue.getReference());
										}
									}
								} else if (subAddress.contains("pullrequest")) {
									if (fromUser != null) {
										involvedPullRequest = pullRequestService.get(entityId);
										if (involvedPullRequest == null)
											throw new ExplicitException("Non-existent pull request specified in recipient address: " + parsedReceiverAddress);
										if (subAddress.contains("unsubscribe")) {
											PullRequestWatch watch = pullRequestWatchService.find(involvedPullRequest, fromUser);
											if (watch != null) 
												watch.setWatching(false);
											
											String subject = "Unsubscribed successfully from pull request " + involvedPullRequest.getReference().toString(null);

											String template = StringUtils.join(settingService.getEmailTemplates().getPullRequestNotificationUnsubscribed(), "\n");
											Map<String, Object> bindings = new HashMap<>();
											bindings.put("pullRequest", involvedPullRequest);
											String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
											String textBody = EmailTemplates.evalTemplate(false, template, bindings);
											var threadingReferences = getThreadingReferences(UUID.randomUUID().toString(), getMessageId(message));
											sendMailAsync(newArrayList(fromInternetAddress.getAddress()), newArrayList(), newArrayList(),
													subject, htmlBody, textBody, null, null, threadingReferences);
										} else {
											if (!SecurityUtils.canReadCode(involvedPullRequest.getProject())) {
												addComment(involvedPullRequest, message, fromInternetAddress, fromUser, receiverInternetAddresses);
											} else {
												throw new UnauthorizedException("Code read permission required for project: %s" 
														+ involvedPullRequest.getProject().getPath());
											}
										}
									} else {
										throw new ExplicitException("No account found with verified email address: " + fromInternetAddress.getAddress());
									}
								} else {
									throw new ExplicitException("Invalid recipient address: " + parsedReceiverAddress);
								}
							}
						} else {
							logger.warn("Ignored recipient '" + parsedReceiverAddress + "' as issue or pull request is processed");
						}
					} else if (!receiverInternetAddress.equals(fromInternetAddress)) {
						involvedInternetAddresses.add(receiverInternetAddress);
					}

					if (involvedIssue != null) {
						for (InternetAddress involvedInternetAddress : involvedInternetAddresses) {
							EmailAddress involvedAddressEntity = emailAddressService.findByValue(involvedInternetAddress.getAddress());
							if (involvedAddressEntity != null && involvedAddressEntity.isVerified()) {
								var involvedUser = involvedAddressEntity.getOwner();
								if (SecurityUtils.canAccessProject(involvedUser.asSubject(), involvedIssue.getProject())) {
									if (involvedIssue.isConfidential())
										issueAuthorizationService.authorize(involvedIssue, involvedUser);
									issueWatchService.watch(involvedIssue, involvedUser, true);
								} else {
									involvedIssue.getExternalParticipants().add(involvedInternetAddress);
								}
							} else {
								involvedIssue.getExternalParticipants().add(involvedInternetAddress);
							}
						}
					} else if (involvedPullRequest != null) {
						for (InternetAddress involvedInternetAddress : involvedInternetAddresses) {
							EmailAddress involvedAddressEntity = emailAddressService.findByValue(involvedInternetAddress.getAddress());
							if (involvedAddressEntity != null && involvedAddressEntity.isVerified()) {
								var involvedUser = involvedAddressEntity.getOwner();
								if (SecurityUtils.canReadCode(involvedUser.asSubject(), involvedPullRequest.getProject())) {
									pullRequestWatchService.watch(involvedPullRequest, involvedUser, true);
								} else {
									logger.warn("Code read permission required to watch pull request (user: {}, project: {})", 
											involvedUser.getDisplayName(), involvedPullRequest.getProject().getPath());
								}
							} else {
								logger.warn("Account with verified email address and code read permission required to watch pull request (email address: {}, project: {})", 
										involvedInternetAddress.getAddress(), involvedPullRequest.getProject().getPath());
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
							Collection<InternetAddress> receiverInternetAddresses) {
		IssueComment comment = new IssueComment();
		comment.setIssue(issue);
		if (author == null) {
			comment.setUser(userService.getSystem());
			comment.setOnBehalfOf(authorInternetAddress);
		} else {
			comment.setUser(author);
		}
		String content = parseBody(message, issue.getProject(), issue.getUUID());
		if (content != null) {
			// Add double line breaks in the beginning and ending as otherwise plain text content 
			// received from email may not be formatted correctly with our markdown renderer. 
			comment.setContent(decorateContent(content));
			var notifiedEmailAddresses = receiverInternetAddresses.stream().map(InternetAddress::getAddress).collect(toSet());
			notifiedEmailAddresses.add(authorInternetAddress.getAddress());
			issueCommentService.create(comment, notifiedEmailAddresses);
		}
	}
	
	private void addComment(PullRequest pullRequest, Message message, InternetAddress authorInternetAddress, 
							User author, Collection<InternetAddress> receiverInternetAddresses) {
		PullRequestComment comment = new PullRequestComment();
		comment.setUser(author);
		String content = parseBody(message, pullRequest.getProject(), pullRequest.getUUID());
		if (content != null) {
			comment.setContent(decorateContent(content));
			var notifiedEmailAddresses = receiverInternetAddresses.stream().map(InternetAddress::getAddress).collect(toSet());
			notifiedEmailAddresses.add(authorInternetAddress.getAddress());
			pullRequestCommentService.create(comment, notifiedEmailAddresses);
		}
	}
	
	private Issue openIssue(Message message, Project project, InternetAddress submitterInternetAddress, 
							@Nullable User submitter, ParsedEmailAddress parsedSystemAddress, 
							Collection<InternetAddress> receiverInternetAddresses) {
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
		
		if (submitter == null) {
			issue.setSubmitter(userService.getSystem());
			issue.setOnBehalfOf(submitterInternetAddress);
			issue.getExternalParticipants().add(submitterInternetAddress);
		} else {
			issue.setSubmitter(submitter);
		}
		
		GlobalIssueSetting issueSetting = settingService.getIssueSetting();
		issue.setState(issueSetting.getInitialStateSpec().getName());
		
		IssueCreationSetting issueCreationSetting = settingService.getServiceDeskSetting().getIssueCreationSetting(project);
		issue.setConfidential(issueCreationSetting.isConfidential());
		for (FieldInstance instance: issueCreationSetting.getIssueFields()) {
			Object fieldValue = issueSetting.getFieldSpec(instance.getName())
					.convertToObject(instance.getValueProvider().getValue());
			issue.setFieldValue(instance.getName(), fieldValue);
		}

		var notifyEmailAddresses = receiverInternetAddresses.stream().map(InternetAddress::getAddress).collect(toSet());
		notifyEmailAddresses.add(submitterInternetAddress.getAddress());
		
		issueService.open(issue, notifyEmailAddresses);
		
		ParsedEmailAddress parsedSubmitterAddress = ParsedEmailAddress.parse(submitterInternetAddress.getAddress());
		if (!parsedSubmitterAddress.getDomain().equalsIgnoreCase(parsedSystemAddress.getDomain()) 
				|| !parsedSubmitterAddress.getName().toLowerCase().startsWith(parsedSystemAddress.getName().toLowerCase() + "+") 
						&& !parsedSubmitterAddress.getName().equalsIgnoreCase(parsedSystemAddress.getName())) {
			String template = StringUtils.join(settingService.getEmailTemplates().getServiceDeskIssueOpened(), "\n");
			Map<String, Object> bindings = new HashMap<>();
			bindings.put("issue", issue);
			String htmlBody = EmailTemplates.evalTemplate(true, template, bindings);
			String textBody = EmailTemplates.evalTemplate(false, template, bindings);
			
			var replyAddress = parsedSystemAddress.getSubaddress("issue~" + issue.getId());
			sendMailAsync(notifyEmailAddresses, newArrayList(), newArrayList(),
					"Re: " + issue.getTitle(), htmlBody, textBody, replyAddress, 
					submitterInternetAddress.getPersonal(), issue.getThreadingReferences()); 
		}
		return issue;
	}
	
	@Listen
	public void on(SystemStarted event) {
		clusterService.getHazelcastInstance().getCluster().addMembershipListener(new MembershipListener() {

			@Override
			public void memberAdded(MembershipEvent membershipEvent) {
			}

			@Override
			public void memberRemoved(MembershipEvent membershipEvent) {
				if (clusterService.isLeaderServer()) {
					Thread copy = thread;
					if (copy != null)
						copy.interrupt();
				}
			}
			
		});
		thread = new Thread(() -> {
			while (thread != null) {
				try {
					var mailConnector = settingService.getMailConnector();
					var inboxMonitor = mailConnector != null? mailConnector.getInboxMonitor(false): null;
					if (inboxMonitor != null && clusterService.isLeaderServer()) {
						var systemAddress = mailConnector.getSystemAddress();
						while (thread != null) {
							Future<?> future = inboxMonitor.monitor(message -> {
								handleMessage(message, systemAddress);
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
								
								String template = StringUtils.join(settingService.getEmailTemplates().getServiceDeskIssueOpenFailed(), "\n");
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
		var mailConnector = settingService.getMailConnector();
		if (mailConnector != null && mailConnector.getInboxMonitor(false) != null)
			return ParsedEmailAddress.parse(mailConnector.getSystemAddress()).getSubaddress(subAddress);
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
			var attachmentService = OneDev.getInstance(AttachmentService.class);
			String attachmentName = attachmentService.saveAttachment(project.getId(), attachmentGroup,
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

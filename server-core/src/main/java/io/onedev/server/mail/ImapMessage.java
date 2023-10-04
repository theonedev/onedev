package io.onedev.server.mail;

import com.google.common.base.Joiner;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.unbescape.html.HtmlEscape;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImapMessage implements MailMessage {
	
	private final Message message;
	
	public ImapMessage(Message message) {
		this.message = message;
	}
	
	@Nullable
	@Override
	public String getId() {
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

	@Override
	public String getSubject() {
		try {
			return message.getSubject();
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<InternetAddress> getToAddresses() {
		return getAddresses("To");
	}

	@Override
	public List<InternetAddress> getCcAddresses() {
		return getAddresses("Cc");
	}

	@Override
	public InternetAddress getFromAddress() {
		var addresses = getAddresses("From");
		return !addresses.isEmpty()? addresses.iterator().next(): null;
	}
	
	private List<InternetAddress> getAddresses(String header) {
		try {
			var addresses = new ArrayList<InternetAddress>();
			String[] toHeader = message.getHeader(header);
			if (toHeader != null && toHeader.length != 0)
				addresses.addAll(Arrays.asList(InternetAddress.parse(toHeader[0], true)));
			return addresses;
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	@Override
	public String parseBody(Project project, String attachmentGroup) {
		try {
			Attachments attachments = new Attachments();
			fillAttachments(project, attachmentGroup, message, attachments);
			String body = parseBody(project, attachmentGroup, message, attachments);

			attachments.identifiable.keySet().removeAll(attachments.referenced);
			attachments.nonIdentifiable.addAll(attachments.identifiable.values());
			if (!attachments.nonIdentifiable.isEmpty()) {
				body += "\n\n---";
				List<String> markdowns = new ArrayList<>();
				for (Attachment attachment: attachments.nonIdentifiable)
					markdowns.add(attachment.getMarkdown());
				body += "\n\n" + Joiner.on(" &nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp; ").join(markdowns);
			}
			return OneDev.getInstance(MailManager.class).stripQuotationAndSignature(body);
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

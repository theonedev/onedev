package io.onedev.server.ee.sendgrid;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.commons.fileupload.MultipartStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultMessageManager implements MessageManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMessageManager.class);
	
	private final List<MessageTarget> messageTargets = new ArrayList<>();
	
	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) {
		var secret = request.getPathInfo().substring(1);
		boolean hasAuthorizedTarget;
		synchronized (messageTargets) {
			hasAuthorizedTarget = messageTargets.stream().anyMatch(it -> it.getSecret().equals(secret));
		}
		if (hasAuthorizedTarget) {
			var contentType = request.getContentType();
			var contentTypePrefix = "multipart/form-data; boundary=";
			Preconditions.checkState(contentType.startsWith(contentTypePrefix));
			var boundary = contentType.substring(contentTypePrefix.length());
			try {
				Message message = null;
				var stream = new MultipartStream(request.getInputStream(), boundary.getBytes(UTF_8));
				var nextPart = stream.skipPreamble();
				while (nextPart) {
					boolean emailPart = false;
					for (var header: Splitter.on('\n').trimResults().split(stream.readHeaders())) {
						if (header.equals("Content-Disposition: form-data; name=\"email\"")) {
							emailPart = true;
							break;
						}
					}
					var baos = new ByteArrayOutputStream();
					stream.readBodyData(baos);
					if (emailPart) {
						var session = Session.getInstance(new Properties());
						message = new MimeMessage(session, new ByteArrayInputStream(baos.toByteArray()));
						break;
					}
					nextPart = stream.readBoundary();
				}	
				
				if (message == null) {
					var errorMessage = "Raw email data not found, please makes sure to enable option 'Post the raw, full MIME message' at SendGrid side for corresponding inbound parser";
					logger.error(errorMessage);
					response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
					response.getWriter().print(errorMessage);
				} else {
					List<SynchronousQueue<Message>> authorizedQueues;
					synchronized (messageTargets) {
						authorizedQueues = messageTargets.stream()
								.filter(it -> it.getSecret().equals(secret))
								.map(MessageTarget::getQueue)
								.collect(toList());
					}
					for (var queue: authorizedQueues)
						queue.offer(message, 60, TimeUnit.SECONDS);
				}
			} catch (IOException | MessagingException | InterruptedException e) {
				throw new RuntimeException(e);
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			var errorMessage = "Incoming message processing not enabled or secret not matching";
			logger.error(errorMessage);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			try {
				response.getWriter().print(errorMessage);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void register(MessageTarget target) {
		synchronized (messageTargets) {
			messageTargets.add(target);
		}
	}

	@Override
	public void unregister(MessageTarget target) {
		synchronized (messageTargets) {
			messageTargets.remove(target);
		}
	}

}

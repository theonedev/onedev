package io.onedev.server.web.component.markdown;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.util.crypt.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.persistence.annotation.Sessional;

@SuppressWarnings("serial")
@Singleton
public class AttachmentUploadServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentUploadServlet.class);
	
	@Sessional
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = URLDecoder.decode(request.getHeader("File-Name"), StandardCharsets.UTF_8.name());
		AttachmentSupport attachmentSuppport = (AttachmentSupport) SerializationUtils
				.deserialize(Base64.decodeBase64(request.getHeader("Attachment-Support")));
		try {
			String attachmentName = attachmentSuppport.saveAttachment(fileName, request.getInputStream());
			response.getWriter().print(URLEncoder.encode(attachmentName, StandardCharsets.UTF_8.name()));
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.error("Error uploading attachment.", e);
			if (e.getMessage() != null)
				response.getWriter().print(e.getMessage());
			else
				response.getWriter().print("Internal server error");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
}

package com.pmease.commons.wicket.behavior.markdown;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.util.crypt.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class AttachmentUploadServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(AttachmentUploadServlet.class);
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = request.getHeader("File-Name");
		AttachmentSupport attachmentSuppport = (AttachmentSupport) SerializationUtils
				.deserialize(Base64.decodeBase64(request.getHeader("Attachment-Support")));
		try {
			String attachmentName = attachmentSuppport.saveAttachment(fileName, request.getInputStream());
			response.getWriter().print(attachmentName);
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

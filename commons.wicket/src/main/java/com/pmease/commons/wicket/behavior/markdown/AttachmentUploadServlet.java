package com.pmease.commons.wicket.behavior.markdown;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.util.crypt.Base64;

@SuppressWarnings("serial")
public class AttachmentUploadServlet extends HttpServlet {

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = request.getHeader("File-Name");
		if (fileName == null) {
			request.getHeader("Content-Type");
		}
		AttachmentSupport attachmentSuppport = (AttachmentSupport) SerializationUtils
				.deserialize(Base64.decodeBase64(request.getHeader("Attachment-Support")));
		String attachmentName = attachmentSuppport.saveAttachment(fileName, request.getInputStream());
		response.getWriter().print(attachmentSuppport.getAttachmentUrl(attachmentName));
		response.setStatus(HttpServletResponse.SC_OK);
	}
	
}

package io.onedev.server.mail;

import io.onedev.server.model.Project;

import javax.annotation.Nullable;
import javax.mail.internet.InternetAddress;
import java.util.List;

public interface MailMessage {
	
	@Nullable
	String getId();
	
	@Nullable
	String getSubject();
	
	List<InternetAddress> getToAddresses();

	List<InternetAddress> getCcAddresses();
	
	InternetAddress getFromAddress();
	
	@Nullable
	String parseBody(Project project, String attachmentGroup);
	
}

package com.pmease.commons.wicket.behavior.markdown;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.form.upload.FileUpload;

public interface AttachmentSupport extends Serializable {
	
	int getAttachmentMaxSize();
	
	List<String> getAttachments();
	
	String getAttachmentUrl(String attachment);
	
	String saveAttachment(FileUpload upload);
	
	void deleteAttachemnt(String attachment);
}

package io.onedev.server.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageManager;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.resource.AttachmentResource;
import io.onedev.server.web.resource.AttachmentResourceReference;

@JsonTypeInfo(use=Id.CLASS)
public class ProjectAttachmentSupport implements AttachmentSupport {

	private static final long serialVersionUID = 1L;

	private static final int MAX_FILE_SIZE = 20*1024*1024; // mega bytes
	
	private static final int BUFFER_SIZE = 1024*64;
	
	private final Long projectId;
	
	private final String attachmentDirUUID;
	
	private final boolean canDeleteAttachment;
	
	public ProjectAttachmentSupport(Project project, String attachmentDirUUID, boolean canDeleteAttachment) {
		projectId = project.getId();
		this.attachmentDirUUID = attachmentDirUUID;
		this.canDeleteAttachment = canDeleteAttachment;
	}
	
	@Override
	public String getAttachmentUrl(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getProject(), attachmentDirUUID, attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}
	
	@Override
	public List<String> getAttachments() {
		List<String> attachments = new ArrayList<>();
		File attachmentDir = OneDev.getInstance(AttachmentStorageManager.class).getAttachmentStorage(getProject(), attachmentDirUUID);
		if (attachmentDir.exists()) {
			for (File file: attachmentDir.listFiles())
				attachments.add(file.getName());
		}
		return attachments;
	}

	private File getAttachmentDir() {
		return OneDev.getInstance(AttachmentStorageManager.class).getAttachmentStorage(getProject(), attachmentDirUUID);
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		File attachmentDir = OneDev.getInstance(AttachmentStorageManager.class).getAttachmentStorage(getProject(), attachmentDirUUID);
		FileUtils.deleteFile(new File(attachmentDir, attachment));
	}

	@Override
	public long getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	protected Project getProject() {
		SessionManager sessionManager = OneDev.getInstance(SessionManager.class);
		sessionManager.openSession();
		try {
			return OneDev.getInstance(ProjectManager.class).load(projectId);
		} finally {
			sessionManager.closeSession();
		}
	}

	@Override
	public String saveAttachment(String suggestedAttachmentName, InputStream attachmentStream) {
		Preconditions.checkState(SecurityUtils.canReadCode(getProject()));
		
		String attachmentName = suggestedAttachmentName;
		File attachmentDir = getAttachmentDir();
		FileUtils.createDir(attachmentDir);
		int index = 2;
		while (new File(attachmentDir, attachmentName).exists()) {
			if (suggestedAttachmentName.contains(".")) {
				String nameBeforeExt = StringUtils.substringBeforeLast(suggestedAttachmentName, ".");
				String ext = StringUtils.substringAfterLast(suggestedAttachmentName, ".");
				attachmentName = nameBeforeExt + "_" + index + "." + ext;
			} else {
				attachmentName = suggestedAttachmentName + "_" + index;
			}
			index++;
		}
		
		Exception ex = null;
		File file = new File(attachmentDir, attachmentName);
		try (OutputStream os = new FileOutputStream(file)) {
			byte[] buffer = new byte[BUFFER_SIZE];
	        long count = 0;
	        int n = 0;
	        while (-1 != (n = attachmentStream.read(buffer))) {
	            count += n;
		        if (count > getAttachmentMaxSize()) {
		        	throw new RuntimeException("Upload must be less than " 
		        			+ FileUtils.byteCountToDisplaySize(getAttachmentMaxSize()));
		        }
	            os.write(buffer, 0, n);
	        }
		} catch (Exception e) {
			ex = e;
		} 
		if (ex != null) {
			if (file.exists())
				FileUtils.deleteFile(file);
			throw ExceptionUtils.unchecked(ex);
		} else {
			return file.getName();
		}
	}

	@Override
	public boolean canDeleteAttachment() {
		return canDeleteAttachment;
	}
	
}

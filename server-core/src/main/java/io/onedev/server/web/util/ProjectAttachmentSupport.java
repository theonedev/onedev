package io.onedev.server.web.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.Preconditions;

import io.onedev.commons.utils.FileUtils;
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
	
	private final Long projectId;
	
	private final String attachmentGroup;
	
	private final boolean canDeleteAttachment;
	
	public ProjectAttachmentSupport(Project project, String attachmentGroup, boolean canDeleteAttachment) {
		projectId = project.getId();
		this.attachmentGroup = attachmentGroup;
		this.canDeleteAttachment = canDeleteAttachment;
	}
	
	@Override
	public String getAttachmentUrlPath(String attachment) {
		PageParameters params = AttachmentResource.paramsOf(getProject(), attachmentGroup, attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}
	
	@Override
	public List<String> getAttachments() {
		List<String> attachments = new ArrayList<>();
		File attachmentDir = getAttachmentDir();
		if (attachmentDir.exists()) {
			for (File file: attachmentDir.listFiles())
				attachments.add(file.getName());
		}
		return attachments;
	}

	private File getAttachmentDir() {
		return OneDev.getInstance(AttachmentStorageManager.class).getGroupDir(getProject(), attachmentGroup);
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		FileUtils.deleteFile(new File(getAttachmentDir(), attachment));
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
		return getProject().saveAttachment(attachmentGroup, suggestedAttachmentName, attachmentStream);
	}

	@Override
	public boolean canDeleteAttachment() {
		return canDeleteAttachment;
	}
	
}

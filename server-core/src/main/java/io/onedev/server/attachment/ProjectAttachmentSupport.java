package io.onedev.server.attachment;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.onedev.server.OneDev;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionService;
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
		PageParameters params = AttachmentResource.paramsOf(getProject().getId(), attachmentGroup, attachment);
		return RequestCycle.get().urlFor(new AttachmentResourceReference(), params).toString();
	}

	@Override
	public List<String> getAttachments() {
		return getAttachmentService().listAttachments(projectId, attachmentGroup).stream()
				.map(it->it.getPath()).collect(Collectors.toList());
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}

	private AttachmentService getAttachmentService() {
		return OneDev.getInstance(AttachmentService.class);
	}
	
	@Override
	public void deleteAttachemnt(String attachment) {
		getAttachmentService().deleteAttachment(projectId, attachmentGroup, attachment);
	}

	@Override
	public long getAttachmentMaxSize() {
		return MAX_FILE_SIZE;
	}

	protected Project getProject() {
		SessionService sessionService = OneDev.getInstance(SessionService.class);
		sessionService.openSession();
		try {
			return getProjectService().load(projectId);
		} finally {
			sessionService.closeSession();
		}
	}

	@Override
	public String saveAttachment(String suggestedAttachmentName, InputStream attachmentStream) {
		return getAttachmentService().saveAttachment(
				projectId, attachmentGroup, suggestedAttachmentName, attachmentStream);
	}

	@Override
	public boolean canDeleteAttachment() {
		return canDeleteAttachment;
	}
	
}

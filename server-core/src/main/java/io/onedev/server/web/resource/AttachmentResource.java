package io.onedev.server.web.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.io.IOUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.storage.AttachmentStorageManager;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_UUID = "uuid";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectName = params.get(PARAM_PROJECT).toString();
		if (StringUtils.isBlank(projectName))
			throw new IllegalArgumentException("project name has to be specified");
		
		Project project = OneDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project: " + projectName);
		
		if (!SecurityUtils.canReadCode(project)) 
			throw new UnauthorizedException();

		String uuid = params.get(PARAM_UUID).toString();
		if (StringUtils.isBlank(uuid))
			throw new IllegalArgumentException("uuid parameter has to be specified");

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		File attachmentFile = new File(getAttachmentDir(project, uuid), attachment);
		if (!attachmentFile.exists()) 
			throw new RuntimeException("Attachment not found: " + attachment);
		
		ResourceResponse response = new ResourceResponse();
		response.setContentLength(attachmentFile.length());
		try {
			response.setContentType(Files.probeContentType(attachmentFile.toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		response.setFileName(attachment);
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				try (InputStream is = new FileInputStream(attachmentFile);) {
					IOUtils.copy(is, attributes.getResponse().getOutputStream());
				}
			}
			
		});

		return response;
	}

	private static File getAttachmentDir(Project project, String uuid) {
		return OneDev.getInstance(AttachmentStorageManager.class).getAttachmentStorage(project, uuid);		
	}
	
	public static PageParameters paramsOf(Project project, String attachmentStorageUUID, String attachmentName) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		params.set(PARAM_UUID, attachmentStorageUUID);
		params.set(PARAM_ATTACHMENT, attachmentName);
		final File attachmentFile = new File(getAttachmentDir(project, attachmentStorageUUID), attachmentName);
		params.set("v", attachmentFile.lastModified());
		
		return params;
	}
	
}

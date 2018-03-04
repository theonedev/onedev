package com.turbodev.server.web.util.resource;

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

import com.google.common.base.Preconditions;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.AttachmentManager;
import com.turbodev.server.manager.ProjectManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.util.facade.ProjectFacade;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_UUID = "uuid";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectName = Preconditions.checkNotNull(params.get(PARAM_PROJECT).toString());
		if (StringUtils.isBlank(projectName))
			throw new IllegalArgumentException("project name has to be specified");
		
		Project project = TurboDev.getInstance(ProjectManager.class).find(projectName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project " + projectName);
		
		if (!SecurityUtils.canRead(project)) 
			throw new UnauthorizedException();

		String storage = params.get(PARAM_UUID).toString();
		if (StringUtils.isBlank(storage))
			throw new IllegalArgumentException("uuid parameter has to be specified");

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		File attachmentFile = new File(getAttachmentDir(project.getFacade(), storage), attachment);
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

	private static File getAttachmentDir(ProjectFacade project, String uuid) {
		return TurboDev.getInstance(AttachmentManager.class).getAttachmentDir(project, uuid);		
	}
	
	public static PageParameters paramsOf(ProjectFacade project, String attachmentDirUUID, String attachmentName) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, project.getName());
		params.set(PARAM_UUID, attachmentDirUUID);
		params.set(PARAM_ATTACHMENT, attachmentName);
		final File attachmentFile = new File(getAttachmentDir(project, attachmentDirUUID), attachmentName);
		params.set("v", attachmentFile.lastModified());
		
		return params;
	}
	
}

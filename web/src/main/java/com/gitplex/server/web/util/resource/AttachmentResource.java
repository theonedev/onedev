package com.gitplex.server.web.util.resource;

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
import org.eclipse.jgit.lib.Constants;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.AttachmentManager;
import com.gitplex.server.manager.ProjectManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.util.facade.ProjectFacade;
import com.google.common.base.Preconditions;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_UUID = "uuid";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String repoName = Preconditions.checkNotNull(params.get(PARAM_PROJECT).toString());
		if (StringUtils.isBlank(repoName))
			throw new IllegalArgumentException("project name has to be specified");
		
		if (repoName.endsWith(Constants.DOT_GIT_EXT))
			repoName = repoName.substring(0, repoName.length() - Constants.DOT_GIT_EXT.length());
		
		Project project = GitPlex.getInstance(ProjectManager.class).find(repoName);
		
		if (project == null) 
			throw new EntityNotFoundException("Unable to find project " + repoName);
		
		if (!SecurityUtils.canRead(project)) 
			throw new UnauthorizedException();

		String storage = params.get(PARAM_UUID).toString();
		if (StringUtils.isBlank(storage))
			throw new IllegalArgumentException("uuid parameter has to be specified");

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");

		File attachmentFile = new File(getAttachmentDir(project.getFacade(), storage), attachment);
		
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
		return GitPlex.getInstance(AttachmentManager.class).getAttachmentDir(project, uuid);		
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

package com.pmease.gitop.web.page.project.source.blob.renderer;

import javax.persistence.EntityNotFoundException;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.DynamicImageResource;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.exception.AccessDeniedException;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.service.FileBlob;

public class ImageBlobResource extends DynamicImageResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected byte[] getImageData(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		final String username = params.get(PageSpec.USER).toString();
		final String projectName = params.get(PageSpec.PROJECT).toString();
		final String revision = params.get("objectId").toString();
		
		Project project = Gitop.getInstance(ProjectManager.class).findBy(username, projectName);
		if (project == null) {
			throw new EntityNotFoundException("Project " + username + "/" + projectName + " doesn't exist");
		}

		Preconditions.checkState(!Strings.isNullOrEmpty(revision));
		
		String path = PageSpec.getPathFromParams(params);
		Preconditions.checkState(!Strings.isNullOrEmpty(path));
		
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
			throw new AccessDeniedException("Permission denied to access " + project.getPathName() + " for user " + SecurityUtils.getSubject());
		}
		
		FileBlob blob = FileBlob.of(project, revision, path);
		return blob.getData();
	}
}

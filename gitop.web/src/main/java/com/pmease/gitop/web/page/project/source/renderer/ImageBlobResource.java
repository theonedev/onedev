package com.pmease.gitop.web.page.project.source.renderer;

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
import com.pmease.gitop.web.page.project.source.GitBlob;

public class ImageBlobResource extends DynamicImageResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected byte[] getImageData(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		String projectId = params.get("project").toString();
		String revision = params.get("objectId").toString();
		String path = params.get("path").toString();
		
		Long pid = Long.valueOf(projectId);
		Project project = Gitop.getInstance(ProjectManager.class).get(pid);
		Preconditions.checkState(project != null);
		Preconditions.checkState(!Strings.isNullOrEmpty(revision));
		Preconditions.checkState(!Strings.isNullOrEmpty(path));
		
		if (!SecurityUtils.getSubject().isPermitted(ObjectPermission.ofProjectRead(project))) {
			throw new AccessDeniedException("Permission denied to access " + project.getPathName() + " for user " + SecurityUtils.getSubject());
		}
		
		GitBlob blob = GitBlob.of(project, revision, path);
		return blob.getContent();
	}
}

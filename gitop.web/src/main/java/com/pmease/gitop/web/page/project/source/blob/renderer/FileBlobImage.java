package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.service.FileBlob;

public class FileBlobImage extends NonCachingImage {
	private static final long serialVersionUID = 1L;

	public static PageParameters newParams(FileBlob blob) {
		PageParameters params = new PageParameters();
		Long projectId = blob.getProjectId();
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		
		params.set(PageSpec.USER, project.getOwner().getName());
		params.set(PageSpec.PROJECT, project.getName());
		params.set("objectId", blob.getRevision());
		PageSpec.addPathToParameters(blob.getPath(), params);
		
		return params;
	}
	
	public FileBlobImage(String id, PageParameters params) {
		super(id, new ImageBlobResourceReference(), params);
	}

	@Override
	protected boolean getStatelessHint() {
		return true;
	}
}

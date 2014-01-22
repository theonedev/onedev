package com.pmease.gitop.web.page.project.source.blob.renderer;

import org.apache.wicket.markup.ComponentTag;
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
		Long projectId = blob.getProjectId();
		Project project = Gitop.getInstance(ProjectManager.class).get(projectId);
		
		return newParams(project, blob.getRevision(), blob.getFilePath());
	}
	
	public static PageParameters newParams(Project project, String revision, String path) {
		PageParameters params = PageSpec.forProject(project);
		params.set(PageSpec.OBJECT_ID, revision);
		PageSpec.addPathToParameters(path, params);
		return params;
	}
	
	public FileBlobImage(String id, Project project, String revision, String path) {
		this(id, newParams(project, revision, path));
	}
	
	public FileBlobImage(String id, PageParameters params) {
		super(id, new ImageBlobResourceReference(), params);
	}

	@Override
	protected boolean getStatelessHint() {
		return true;
	}
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);
//
//		ResourceReference rr = getImageResourceReference();
//		if (rr != null) {
//			ImageBlobResourceReference brr = (ImageBlobResourceReference) rr;
//			Dimension d = ((ImageBlobResource) brr.getResource()).getImageDimension();
//			tag.put("width", d.width);
//			tag.put("height", d.height);
//		}
	}
}

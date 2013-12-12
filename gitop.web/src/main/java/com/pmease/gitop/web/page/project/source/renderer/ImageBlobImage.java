package com.pmease.gitop.web.page.project.source.renderer;

import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ImageBlobImage extends NonCachingImage {
	private static final long serialVersionUID = 1L;

	public static PageParameters newParams(Long projectId, String revision, String path) {
		PageParameters params = new PageParameters();
		params.set("project", projectId);
		params.set("objectId", revision);
		params.set("path", path);
		
		return params;
	}
	
	public ImageBlobImage(String id, PageParameters params) {
		super(id, new ImageBlobResourceReference(), params);
	}

	@Override
	protected boolean getStatelessHint() {
		return true;
	}
}

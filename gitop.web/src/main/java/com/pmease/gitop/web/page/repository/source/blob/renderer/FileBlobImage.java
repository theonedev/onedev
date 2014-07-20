package com.pmease.gitop.web.page.repository.source.blob.renderer;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.util.ParamUtils;

public class FileBlobImage extends NonCachingImage {
	private static final long serialVersionUID = 1L;

	public static PageParameters paramsOf(FileBlob blob) {
		Long repositoryId = blob.getRepositoryId();
		Repository repository = Gitop.getInstance(Dao.class).load(Repository.class, repositoryId);
		
		return paramsOf(repository, blob.getRevision(), blob.getFilePath());
	}
	
	public static PageParameters paramsOf(Repository repository, String revision, String path) {
		PageParameters params = RepositoryPage.paramsOf(repository);
		params.set(RepositoryPage.PARAM_OBJECT_ID, revision);
		ParamUtils.addPathToParams(path, params);
		return params;
	}
	
	public FileBlobImage(String id, Repository repository, String revision, String path) {
		this(id, paramsOf(repository, revision, path));
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

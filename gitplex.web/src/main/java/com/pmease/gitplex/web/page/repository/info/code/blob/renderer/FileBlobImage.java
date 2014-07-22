package com.pmease.gitplex.web.page.repository.info.code.blob.renderer;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.service.FileBlob;

public class FileBlobImage extends NonCachingImage {
	private static final long serialVersionUID = 1L;

	public static PageParameters paramsOf(FileBlob blob) {
		Long repositoryId = blob.getRepositoryId();
		Repository repository = GitPlex.getInstance(Dao.class).load(Repository.class, repositoryId);
		
		return RepositoryInfoPage.paramsOf(repository, blob.getRevision(), blob.getFilePath());
	}
	
	public FileBlobImage(String id, Repository repository, String revision, String path) {
		this(id, RepositoryInfoPage.paramsOf(repository, revision, path));
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

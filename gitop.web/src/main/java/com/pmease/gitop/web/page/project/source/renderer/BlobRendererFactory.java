package com.pmease.gitop.web.page.project.source.renderer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.web.page.project.source.GitBlob;
import com.pmease.gitop.web.service.FileTypeRegistry;
import com.pmease.gitop.web.util.MimeTypeUtils;

@Singleton
public class BlobRendererFactory {

	static final TextBlobRenderer TEXT = new TextBlobRenderer();
	static final ImageBlobRenderer IMAGE = new ImageBlobRenderer();
	static final GeneralBlobRenderer GENERAL = new GeneralBlobRenderer();
	
	private final FileTypeRegistry fileTypeRegistry;
	
	@Inject
	BlobRendererFactory(FileTypeRegistry fileTypeRegistry) {
		this.fileTypeRegistry = fileTypeRegistry;
	}
	
	public BlobRenderer create(GitBlob blob) {
		if (fileTypeRegistry.isSafeInline(blob.getMime())) {
			if (MimeTypeUtils.isTextType(blob.getMime())) {
				return TEXT;
			} else if (MimeTypeUtils.isImageType(blob.getMime())) {
				return IMAGE;
			} else {
				throw new UnsupportedOperationException("Mime " + blob.getMime());
			}
		} else {
			return GENERAL;
		}
	}
}

package com.pmease.gitplex.web.page.repository.info.code.blob.renderer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.web.service.FileBlob;
import com.pmease.gitplex.web.service.FileTypes;

@Singleton
public class BlobRendererFactory {

	private static enum Registry {
		TEXT(new TextBlobRenderer()), 
		IMAGE(new ImageBlobRenderer()), 
		RAW(new RawBlobRenderer());
		
		final BlobRenderer renderer;
		Registry(BlobRenderer renderer) {
			this.renderer = renderer;
		}
	}
	
	private final FileTypes fileTypes;
	
	@Inject
	BlobRendererFactory(FileTypes fileTypes) {
		this.fileTypes = fileTypes;
	}
	
	public BlobRenderer newRenderer(FileBlob blob) {
		if (fileTypes.isSafeInline(blob.getMediaType())) {
			if (blob.isImage()) {
				return Registry.IMAGE.renderer;
			} else if (blob.isText() && !blob.isLarge()) {
				return Registry.TEXT.renderer;
			}
		}
		
		return Registry.RAW.renderer;
	}
}

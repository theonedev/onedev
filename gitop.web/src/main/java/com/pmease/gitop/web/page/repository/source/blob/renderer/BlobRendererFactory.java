package com.pmease.gitop.web.page.repository.source.blob.renderer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.web.service.FileBlob;
import com.pmease.gitop.web.service.FileTypes;

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

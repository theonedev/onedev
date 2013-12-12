package com.pmease.gitop.web.page.project.source.blob.renderer;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.web.page.project.source.blob.FileBlob;
import com.pmease.gitop.web.service.FileTypeRegistry;

@Singleton
public class BlobRendererFactory {

	private static enum Registry {
		TEXT(new TextBlobRenderer()), 
		IMAGE(new ImageBlobRenderer()), 
		GENERAL(new GeneralBlobRenderer());
		
		final BlobRenderer renderer;
		Registry(BlobRenderer renderer) {
			this.renderer = renderer;
		}
	}
	
	private final FileTypeRegistry fileTypeRegistry;
	
	@Inject
	BlobRendererFactory(FileTypeRegistry fileTypeRegistry) {
		this.fileTypeRegistry = fileTypeRegistry;
	}
	
	public BlobRenderer newRenderer(FileBlob blob) {
		if (!blob.isLarge()
				&& fileTypeRegistry.isSafeInline(blob.getMimeType())) {
			
			if (blob.isText()) {
				return Registry.TEXT.renderer;
			} else if (blob.isImage()) {
				return Registry.IMAGE.renderer;
			} else {
				throw new UnsupportedOperationException("Mime " + blob.getMimeType());
			}
		} else {
			return Registry.GENERAL.renderer;
		}
	}
}

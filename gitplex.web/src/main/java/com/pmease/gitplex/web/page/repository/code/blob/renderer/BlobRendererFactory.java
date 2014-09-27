package com.pmease.gitplex.web.page.repository.code.blob.renderer;

import javax.inject.Singleton;

import com.pmease.commons.util.MediaTypes;
import com.pmease.gitplex.web.service.FileBlob;

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
	
	public BlobRenderer newRenderer(FileBlob blob) {
		if (MediaTypes.isSafeInline(blob.getMediaType())) {
			if (blob.isImage()) {
				return Registry.IMAGE.renderer;
			} else if (blob.isText() && !blob.isLarge()) {
				return Registry.TEXT.renderer;
			}
		}
		
		return Registry.RAW.renderer;
	}
}

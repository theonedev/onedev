package com.pmease.gitplex.web.component.blobview;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface BlobRenderer {

	@Nullable BlobViewPanel render(String panelId, BlobViewContext context);
	
}

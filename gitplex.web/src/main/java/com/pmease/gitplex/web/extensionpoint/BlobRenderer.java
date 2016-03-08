package com.pmease.gitplex.web.extensionpoint;

import javax.annotation.Nullable;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

@ExtensionPoint
public interface BlobRenderer {

	@Nullable BlobViewPanel render(String panelId, BlobViewContext context, @Nullable String clientState);
	
}

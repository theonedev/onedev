package com.pmease.gitplex.web.component.repofile.blobview.image;

import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

public class ImageRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlob().getMediaType().getType().equalsIgnoreCase("image"))
			return new ImageViewPanel(panelId, context);
		else
			return null;
	}

}

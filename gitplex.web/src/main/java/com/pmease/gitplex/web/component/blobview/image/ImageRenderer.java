package com.pmease.gitplex.web.component.blobview.image;

import com.pmease.gitplex.web.component.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

public class ImageRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlob().getMediaType().getType().equalsIgnoreCase("image"))
			return new ImageViewPanel(panelId, context);
		else
			return null;
	}

}

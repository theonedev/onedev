package com.pmease.gitplex.web.component.blobview.binary;

import com.pmease.gitplex.web.component.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

public class BinaryRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlob().getText() == null 
				&& !context.getBlob().getMediaType().getType().equalsIgnoreCase("image")) {
			return new BinaryViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

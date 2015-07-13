package com.pmease.gitplex.web.component.repofile.blobview.binary;

import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

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

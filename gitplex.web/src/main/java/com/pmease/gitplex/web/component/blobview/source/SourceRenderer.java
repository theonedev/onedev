package com.pmease.gitplex.web.component.blobview.source;

import com.pmease.gitplex.web.component.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

public class SourceRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlobIdent().isFile() 
				&& context.getBlob().getText() != null 
				&& !context.getBlobIdent().path.endsWith(".md")) {
			return new SourceViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

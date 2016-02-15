package com.pmease.gitplex.web.component.repofile.blobview.source;

import com.pmease.commons.git.Blob;
import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

public class SourceRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String clientState) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		if (context.getBlobIdent().isFile() 
				&& blob.getText() != null 
				&& !context.getBlobIdent().path.endsWith(".md")) {
			return new SourceViewPanel(panelId, context, clientState);
		} else {
			return null;
		}
	}

}

package com.gitplex.server.web.component.depotfile.blobview.source;

import com.gitplex.server.git.Blob;
import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

public class SourceRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		if (context.getBlobIdent().isFile() 
				&& blob.getText() != null 
				&& !context.getBlobIdent().path.endsWith(".md")) {
			return new SourceViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

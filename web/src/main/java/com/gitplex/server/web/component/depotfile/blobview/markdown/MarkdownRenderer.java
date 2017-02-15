package com.gitplex.server.web.component.depotfile.blobview.markdown;

import com.gitplex.server.git.Blob;
import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext.Mode;
import com.gitplex.server.web.component.depotfile.blobview.source.SourceViewPanel;

public class MarkdownRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		if (context.getBlobIdent().isFile() 
				&& blob.getText() != null 
				&& context.getBlobIdent().path.endsWith(".md")) { 
			if (context.getMark() != null || context.getMode() == Mode.BLAME)
				return new SourceViewPanel(panelId, context);
			else
				return new MarkdownFilePanel(panelId, context);
		} else {
			return null;
		}
	}

}

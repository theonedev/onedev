package com.gitplex.server.web.component.depotfile.blobview.symbollink;

import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

public class SymbolLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlobIdent().isSymbolLink())
			return new SymbolLinkPanel(panelId, context);
		else
			return null;
	}

}

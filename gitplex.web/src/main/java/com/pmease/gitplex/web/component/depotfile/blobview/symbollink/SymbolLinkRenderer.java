package com.pmease.gitplex.web.component.depotfile.blobview.symbollink;

import com.pmease.gitplex.web.component.depotfile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;

public class SymbolLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String viewState) {
		if (context.getBlobIdent().isSymbolLink())
			return new SymbolLinkPanel(panelId, context);
		else
			return null;
	}

}

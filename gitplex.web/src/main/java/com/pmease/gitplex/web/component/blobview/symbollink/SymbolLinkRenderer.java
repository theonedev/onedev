package com.pmease.gitplex.web.component.blobview.symbollink;

import com.pmease.gitplex.web.component.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

public class SymbolLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getState().file.isSymbolLink())
			return new SymbolLinkPanel(panelId, context);
		else
			return null;
	}

}

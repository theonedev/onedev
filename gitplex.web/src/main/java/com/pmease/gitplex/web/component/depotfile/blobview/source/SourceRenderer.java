package com.pmease.gitplex.web.component.depotfile.blobview.source;

import com.pmease.commons.git.Blob;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;

public class SourceRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context == null || context.getDepot() == null) {
			if (context == null)
				System.out.println("context is null");
			else if (context.getDepot() == null)
				System.out.println("context.getDepot() is null");
			System.out.println(Thread.currentThread());
		}
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

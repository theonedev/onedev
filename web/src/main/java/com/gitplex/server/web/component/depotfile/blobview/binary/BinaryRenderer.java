package com.gitplex.server.web.component.depotfile.blobview.binary;

import com.gitplex.commons.git.Blob;
import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

public class BinaryRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		if (blob.getText() == null 
				&& !blob.getMediaType().getType().equalsIgnoreCase("image")) {
			return new BinaryViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

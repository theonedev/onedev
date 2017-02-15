package com.gitplex.server.web.component.depotfile.blobview.image;

import com.gitplex.server.git.Blob;
import com.gitplex.server.web.component.depotfile.blobview.BlobRenderer;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewContext;
import com.gitplex.server.web.component.depotfile.blobview.BlobViewPanel;

public class ImageRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		if (blob.getMediaType().getType().equalsIgnoreCase("image"))
			return new ImageViewPanel(panelId, context);
		else
			return null;
	}

}

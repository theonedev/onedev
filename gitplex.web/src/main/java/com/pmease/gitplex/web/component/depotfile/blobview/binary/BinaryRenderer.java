package com.pmease.gitplex.web.component.depotfile.blobview.binary;

import com.pmease.commons.git.Blob;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;

public class BinaryRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String viewState) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		if (blob.getText() == null 
				&& !blob.getMediaType().getType().equalsIgnoreCase("image")) {
			return new BinaryViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

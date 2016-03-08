package com.pmease.gitplex.web.component.repofile.blobview.binary;

import com.pmease.commons.git.Blob;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.extensionpoint.BlobRenderer;

public class BinaryRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String clientState) {
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		
		if (blob.getText() == null 
				&& !blob.getMediaType().getType().equalsIgnoreCase("image")) {
			return new BinaryViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

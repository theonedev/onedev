package com.pmease.gitplex.web.component.depotfile.blobview.gitlink;

import com.pmease.gitplex.web.component.depotfile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.depotfile.blobview.BlobViewPanel;

public class GitLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String viewState) {
		if (context.getBlobIdent().isGitLink())
			return new GitLinkPanel(panelId, context);
		else
			return null;
	}

}

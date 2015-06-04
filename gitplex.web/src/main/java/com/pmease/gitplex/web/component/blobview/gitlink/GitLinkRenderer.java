package com.pmease.gitplex.web.component.blobview.gitlink;

import com.pmease.gitplex.web.component.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

public class GitLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getBlobIdent().isGitLink())
			return new GitLinkPanel(panelId, context);
		else
			return null;
	}

}

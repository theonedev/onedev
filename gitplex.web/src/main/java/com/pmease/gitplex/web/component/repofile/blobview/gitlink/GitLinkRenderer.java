package com.pmease.gitplex.web.component.repofile.blobview.gitlink;

import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

public class GitLinkRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context, String clientState) {
		if (context.getBlobIdent().isGitLink())
			return new GitLinkPanel(panelId, context);
		else
			return null;
	}

}

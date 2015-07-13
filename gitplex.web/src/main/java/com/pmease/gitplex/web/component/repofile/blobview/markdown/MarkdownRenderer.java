package com.pmease.gitplex.web.component.repofile.blobview.markdown;

import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.repofile.blobview.source.SourceViewPanel;

public class MarkdownRenderer implements BlobRenderer {

	@Override
	public BlobViewPanel render(String panelId, BlobViewContext context) {
		if (context.getState().file.isFile() 
				&& context.getBlob().getText() != null 
				&& context.getState().file.path.endsWith(".md")) { 
			if (context.getState().tokenPos != null || context.getState().blame)
				return new SourceViewPanel(panelId, context);
			else
				return new MarkdownViewPanel(panelId, context);
		} else {
			return null;
		}
	}

}

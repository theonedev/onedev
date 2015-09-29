package com.pmease.gitplex.web.component.repofile.blobview.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.git.Blob;
import com.pmease.commons.wicket.component.markdownviewer.MarkdownViewer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownFilePanel extends BlobViewPanel {

	public MarkdownFilePanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getRepository().getBlob(context.getBlobIdent());
		add(new MarkdownViewer("markdown", Model.of(blob.getText().getContent()), false));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(MarkdownFilePanel.class, "markdown-file.css")));
	}

}

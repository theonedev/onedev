package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;

import com.gitplex.server.git.Blob;
import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownBlobViewPanel extends BlobViewPanel {

	public MarkdownBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		add(new MarkdownViewer("markdown", Model.of(blob.getText().getContent()), null));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobResourceReference()));
	}

	@Override
	protected boolean canEdit() {
		return true;
	}

	@Override
	protected boolean canBlame() {
		return true;
	}

}

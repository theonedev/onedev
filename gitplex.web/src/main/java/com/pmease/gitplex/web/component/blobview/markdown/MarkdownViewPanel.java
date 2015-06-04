package com.pmease.gitplex.web.component.blobview.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.component.markdown.MarkdownPanel;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownViewPanel extends BlobViewPanel {

	public MarkdownViewPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new MarkdownPanel("markdown", Model.of(context.getBlob().getText().getContent())));
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(MarkdownViewPanel.class, "markdown-view.css")));
	}

}

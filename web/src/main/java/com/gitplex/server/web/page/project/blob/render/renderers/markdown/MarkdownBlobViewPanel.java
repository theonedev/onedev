package com.gitplex.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;

import com.gitplex.server.git.Blob;
import com.gitplex.server.web.component.markdown.MarkdownViewer;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class MarkdownBlobViewPanel extends BlobViewPanel {

	public MarkdownBlobViewPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Blob blob = context.getProject().getBlob(context.getBlobIdent());
		add(new MarkdownViewer("markdown", Model.of(blob.getText().getContent()), null) {

			@Override
			protected String getBaseUrl() {
				ProjectBlobPage.State state = new ProjectBlobPage.State(context.getBlobIdent());
				return urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(context.getProject(), state)).toString();
			}
			
		});
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

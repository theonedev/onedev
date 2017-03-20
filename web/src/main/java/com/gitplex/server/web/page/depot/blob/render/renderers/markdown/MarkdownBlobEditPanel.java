package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.depot.blob.render.edit.BlobEditPanel;

@SuppressWarnings("serial")
public class MarkdownBlobEditPanel extends BlobEditPanel {

	public MarkdownBlobEditPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new MarkdownBlobResourceReference()));
	}

	@Override
	protected FormComponentPanel<byte[]> newContentEditor(String componentId, byte[] initialContent) {
		return new MarkdownBlobEditor(componentId, initialContent, context.getMode() == Mode.EDIT) {

			@Override
			protected BlobReferenceSupport getBlobReferenceSupport() {
				return new BlobReferenceSupport() {

					@Override
					public Depot getDepot() {
						return context.getDepot();
					}

					@Override
					public BlobIdent getBaseBlobIdent() {
						return context.getBlobIdent();
					}
					
				};
			}
			
		};
	}

}

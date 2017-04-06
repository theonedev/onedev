package com.gitplex.server.web.page.depot.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.Depot;
import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
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
					public String getRevision() {
						return context.getBlobIdent().revision;
					}

				};
			}

			@Override
			protected String getAutosaveKey() {
				return context.getAutosaveKey();
			}

			@Override
			protected String getBaseUrl() {
				BlobIdent blobIdent = new BlobIdent(context.getBlobIdent().revision, context.getNewPath(), 
						FileMode.REGULAR_FILE.getBits());
				DepotBlobPage.State state = new DepotBlobPage.State(blobIdent);
				return urlFor(DepotBlobPage.class, DepotBlobPage.paramsOf(context.getDepot(), state)).toString();
			}

		};
	}

}

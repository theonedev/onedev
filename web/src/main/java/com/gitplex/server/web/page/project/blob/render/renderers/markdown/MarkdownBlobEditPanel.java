package com.gitplex.server.web.page.project.blob.render.renderers.markdown;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.eclipse.jgit.lib.FileMode;

import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.model.Project;
import com.gitplex.server.web.component.markdown.BlobReferenceSupport;
import com.gitplex.server.web.page.project.blob.ProjectBlobPage;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.project.blob.render.BlobRenderContext.Mode;
import com.gitplex.server.web.page.project.blob.render.edit.BlobEditPanel;

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
					public Project getProject() {
						return context.getProject();
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
				ProjectBlobPage.State state = new ProjectBlobPage.State(blobIdent);
				return urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(context.getProject(), state)).toString();
			}

		};
	}

}

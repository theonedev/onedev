package io.onedev.server.web.page.project.blob.render.renderers.symbollink;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderContext;
import io.onedev.server.web.page.project.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Blob blob = context.getProject().getBlob(context.getBlobIdent(), true);
		String targetPath = PathUtils.normalizeDots(
				PathUtils.resolveSibling(context.getBlobIdent().path, blob.getText().getContent()));
		if (targetPath != null && (targetPath.startsWith("/") || new File(targetPath).isAbsolute())) 
			targetPath = null;

		BlobIdent targetBlobIdent;
		if (targetPath != null) {
			Repository repository = context.getProject().getRepository();				
			try (RevWalk revWalk = new RevWalk(repository)) {
				ObjectId commitId = context.getProject().getObjectId(context.getBlobIdent().revision, true);
				RevTree revTree = revWalk.parseCommit(commitId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, targetPath, revTree);
				if (treeWalk != null) {
					targetBlobIdent = new BlobIdent(context.getBlobIdent().revision, targetPath, treeWalk.getRawMode(0));
				} else {
					targetBlobIdent = null;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			targetBlobIdent = null;
		}

		WebMarkupContainer link;
		if (targetBlobIdent == null) {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		} else {
			ProjectBlobPage.State state = new ProjectBlobPage.State(targetBlobIdent);
			link = new ViewStateAwarePageLink<Void>("link", ProjectBlobPage.class, 
					ProjectBlobPage.paramsOf(context.getProject(), state));
		} 
		link.add(new Label("label", blob.getText().getContent()));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new SymbolLinkResourceReference()));
	}

	@Override
	protected boolean isEditSupported() {
		return false;
	}

	@Override
	protected boolean isViewPlainSupported() {
		return false;
	}

}

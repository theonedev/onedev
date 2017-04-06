package com.gitplex.server.web.page.depot.blob.render.renderers.symbollink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
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

import com.gitplex.server.git.Blob;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.view.BlobViewPanel;

@SuppressWarnings("serial")
public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobRenderContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Blob blob = context.getDepot().getBlob(context.getBlobIdent());
		String targetPath = FilenameUtils.normalize(
				Paths.get(context.getBlobIdent().path).resolveSibling(
						blob.getText().getContent()).toString(), true);
		if (targetPath != null && (targetPath.startsWith("/") || new File(targetPath).isAbsolute())) 
			targetPath = null;

		BlobIdent targetBlobIdent;
		if (targetPath != null) {
			Repository repository = context.getDepot().getRepository();				
			try (RevWalk revWalk = new RevWalk(repository)) {
				ObjectId commitId = context.getDepot().getObjectId(context.getBlobIdent().revision);
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
			DepotBlobPage.State state = new DepotBlobPage.State(targetBlobIdent);
			link = new ViewStateAwarePageLink<Void>("link", DepotBlobPage.class, 
					DepotBlobPage.paramsOf(context.getDepot(), state));
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
	protected boolean canEdit() {
		return false;
	}

	@Override
	protected boolean canBlame() {
		return false;
	}

}

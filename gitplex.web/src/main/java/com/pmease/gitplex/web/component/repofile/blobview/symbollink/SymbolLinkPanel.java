package com.pmease.gitplex.web.component.repofile.blobview.symbollink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.commons.git.Blob;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;

@SuppressWarnings("serial")
public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobViewContext context) {
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

		if (targetPath != null) {
			try (	Repository repository = context.getDepot().openRepository(); 
					RevWalk revWalk = new RevWalk(repository)) {
				ObjectId commitId = context.getDepot().getObjectId(context.getBlobIdent().revision);
				RevTree revTree = revWalk.parseCommit(commitId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, targetPath, revTree);
				if (treeWalk == null)
					targetPath = null;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		WebMarkupContainer link;
		if (targetPath == null) {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		} else {
			RepoFileState state = new RepoFileState();
			state.blobIdent.revision = context.getBlobIdent().revision;
			state.blobIdent.path = targetPath;
			state.requestId = PullRequest.idOf(context.getPullRequest());
			link = new BookmarkablePageLink<Void>("link", RepoFilePage.class, 
					RepoFilePage.paramsOf(context.getDepot(), state));
		} 
		link.add(new Label("label", blob.getText().getContent()));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(SymbolLinkPanel.class, "symbol-link.css")));
	}

}

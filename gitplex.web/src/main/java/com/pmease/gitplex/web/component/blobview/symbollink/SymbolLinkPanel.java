package com.pmease.gitplex.web.component.blobview.symbollink;

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
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String targetPath = FilenameUtils.normalize(
				Paths.get(context.getState().file.path).resolveSibling(
						context.getBlob().getText().getContent()).toString(), true);
		if (targetPath != null && (targetPath.startsWith("/") || new File(targetPath).isAbsolute())) 
			targetPath = null;

		if (targetPath != null) {
			try (	FileRepository jgitRepo = context.getRepository().openAsJGitRepo(); 
					RevWalk revWalk = new RevWalk(jgitRepo)) {
				ObjectId commitId = context.getRepository().getObjectId(context.getState().file.revision, true);
				RevTree revTree = revWalk.parseCommit(commitId).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, targetPath, revTree);
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
			link = new BookmarkablePageLink<Void>("link", RepoFilePage.class, 
					RepoFilePage.paramsOf(context.getRepository(), context.getState().file.revision, targetPath));
		} 
		link.add(new Label("label", context.getBlob().getText().getContent()));
		add(link);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(SymbolLinkPanel.class, "symbol-link.css")));
	}

}

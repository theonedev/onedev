package com.pmease.gitplex.web.component.blobview.symbollink;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;
import com.pmease.gitplex.web.page.repository.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;

@SuppressWarnings("serial")
public class SymbolLinkPanel extends BlobViewPanel {

	public SymbolLinkPanel(String id, BlobViewContext context) {
		super(id, context);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TreeNode targetNode;
		String normalizedTarget = FilenameUtils.normalize(
				Paths.get(context.getBlobIdent().path).resolveSibling(context.getBlob().getText().getContent()).toString(), true);
		if (normalizedTarget == null || normalizedTarget.startsWith("/") || new File(normalizedTarget).isAbsolute()) {
			targetNode = null;
		} else {
			List<TreeNode> result = context.getRepository().git().listTree(context.getBlobIdent().revision, normalizedTarget);
			if (result != null)
				targetNode = result.iterator().next();
			else
				targetNode = null;
		}

		WebMarkupContainer link;
		if (targetNode == null) {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		} else if (targetNode.getMode() == FileMode.TYPE_TREE) {
			link = new BookmarkablePageLink<Void>("link", RepoFilePage.class, 
					RepoFilePage.paramsOf(context.getRepository(), context.getBlobIdent().revision, targetNode.getPath()));
		} else if ((targetNode.getMode()&FileMode.TYPE_MASK) == FileMode.TYPE_FILE) {
			link = new BookmarkablePageLink<Void>("link", RepoBlobPage.class, 
					RepoFilePage.paramsOf(context.getRepository(), context.getBlobIdent().revision, targetNode.getPath()));
		} else {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		}
		link.add(new Label("label", context.getBlob().getText().getContent()));
		add(link);
	}

}

package com.pmease.gitplex.web.component.symbollink;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.code.tree.RepoTreePage;

@SuppressWarnings("serial")
public class SymbolLink extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String revision;
	
	private final String linkPath;
	
	private final String targetPath;
	
	public SymbolLink(String id, IModel<Repository> repoModel, String revision, 
			String linkPath, String targetPath) {
		super(id);
		
		this.repoModel = repoModel;
		this.revision = revision;
		this.linkPath = linkPath;
		this.targetPath = targetPath;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		TreeNode targetNode;
		String normalizedTarget = FilenameUtils.normalize(
				Paths.get(linkPath).resolveSibling(targetPath).toString(), true);
		if (normalizedTarget == null || normalizedTarget.startsWith("/") || new File(normalizedTarget).isAbsolute()) {
			targetNode = null;
		} else {
			List<TreeNode> result = repoModel.getObject().git().listTree(revision, normalizedTarget);
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
			link = new BookmarkablePageLink<Void>("link", RepoTreePage.class, 
					RepoTreePage.paramsOf(repoModel.getObject(), revision, targetNode.getPath()));
		} else if ((targetNode.getMode()&FileMode.TYPE_MASK) == FileMode.TYPE_FILE) {
			link = new BookmarkablePageLink<Void>("link", RepoBlobPage.class, 
					RepoTreePage.paramsOf(repoModel.getObject(), revision, targetNode.getPath()));
		} else {
			link = new Link<Void>("link") {

				@Override
				public void onClick() {
				}
				
			};
			link.setEnabled(false);
		}
		link.add(new Label("label", targetPath));
		add(link);
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

}

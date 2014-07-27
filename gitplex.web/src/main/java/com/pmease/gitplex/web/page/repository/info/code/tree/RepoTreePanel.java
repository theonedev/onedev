package com.pmease.gitplex.web.page.repository.info.code.tree;

import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.collect.Iterables;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.PathUtils;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.commit.CommitMetaPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.info.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.info.code.commits.RepoCommitsPage;

@SuppressWarnings("serial")
public class RepoTreePanel extends Panel {
	
	private final IModel<Repository> repoModel;
	
	private final String currentRevision;
	
	private final String currentPath;
	
	private final IModel<Commit> lastCommitModel;
	
	private final IModel<List<TreeNode>> nodesModel;
	
	public RepoTreePanel(String id, final IModel<Repository> repoModel, final @Nullable String currentRevision, 
			final @Nullable String currentPath, IModel<List<TreeNode>> nodesModel) {
		super(id);
		
		this.repoModel = repoModel;
		this.currentRevision = currentRevision;
		this.currentPath = currentPath;
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = repoModel.getObject().git();
				String revision = repoModel.getObject().defaultBranchIfNull(RepoTreePanel.this.currentRevision);
				List<Commit> commits = git.log(null, revision, currentPath, 1, 0);
				Commit c = Iterables.getFirst(commits, null);
				if (c == null) {
					throw new EntityNotFoundException("Unable to get commit for revision " + revision);
				} else {
					return c;
				}
			}
		};
		
		this.nodesModel = nodesModel;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new CommitMessagePanel("message", repoModel, lastCommitModel));
		add(new CommitMetaPanel("meta", lastCommitModel).setAuthorMode(AvatarMode.NAME_AND_AVATAR));
		
		BookmarkablePageLink<Void> historyLink = new BookmarkablePageLink<Void>(
				"history",
				RepoCommitsPage.class,
				RepoCommitsPage.paramsOf(repoModel.getObject(), currentRevision, currentPath, 0));
		
		add(historyLink);
		BookmarkablePageLink<Void> commitLink = new BookmarkablePageLink<Void>(
				"commitlink",
				RepoCommitPage.class,
				RepoCommitPage.paramsOf(repoModel.getObject(), getLastCommit().getHash(), null));
		add(commitLink);
		commitLink.add(new Label("sha", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.abbreviateSHA(getLastCommit().getHash());
			}
		}));
		
		List<String> pathElements = PathUtils.split(currentPath);
		if (pathElements.isEmpty()) {
			add(new WebMarkupContainer("parent").setVisibilityAllowed(false));
		} else {
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("parent",
					RepoTreePage.class,
					RepoTreePage.paramsOf(repoModel.getObject(), currentRevision, PathUtils.join(pathElements.subList(0, pathElements.size() - 1))));
			
			add(link);
		}
		
		ListView<TreeNode> fileTree = new ListView<TreeNode>("files", nodesModel) {

			@Override
			protected void populateItem(ListItem<TreeNode> item) {
				TreeNode node = item.getModelObject();
				final int bits = node.getMode().getBits();
				final String path = node.getPath();
				Icon icon = new Icon("icon", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						FileMode mode = FileMode.fromBits(bits);
						if (mode == FileMode.TREE) 
							return "icon-folder";
						else if (mode == FileMode.GITLINK)
							return "icon-folder-submodule";
						else if (mode == FileMode.SYMLINK) {
							Git git = repoModel.getObject().git();
							if (git.isTreeLink(path, repoModel.getObject().defaultBranchIfNull(currentRevision)))
								return "icon-folder-symlink";
							else
								return "icon-file-symlink";
						} else 
							return "icon-file-general";
					}
				});
				
				item.add(icon);
				
				List<String> pathElements = PathUtils.split(currentPath);
				pathElements.add(node.getName());
				
				PageParameters params = RepositoryInfoPage.paramsOf(repoModel.getObject(), 
						currentRevision, PathUtils.join(pathElements));
				
				AbstractLink link;
				FileMode mode = node.getMode();
				if (mode == FileMode.TREE) {
					link = new BookmarkablePageLink<Void>("file", RepoTreePage.class, params);
				} else {
					link = new BookmarkablePageLink<Void>("file", RepoBlobPage.class, params);					
				}
				
				link.add(new Label("name", node.getName()));
				item.add(link);
			}
		};
		
		add(fileTree);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(OnDomReadyHeaderItem.forScript("gitplex.commitMessage.toggle('.title-message .detailed-toggle');"));
	}
	
	private Commit getLastCommit() {
		return lastCommitModel.getObject();
	}
	
	@Override
	public void onDetach() {
		repoModel.detach();
		lastCommitModel.detach();
		nodesModel.detach();
		
		super.onDetach();
	}
}

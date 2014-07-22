package com.pmease.gitplex.web.page.repository.info.code.tree;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.web.common.wicket.bootstrap.Icon;
import com.pmease.gitplex.web.component.commit.CommitMessagePanel;
import com.pmease.gitplex.web.component.commit.CommitMetaPanel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.page.repository.info.RepoInfoPanel;
import com.pmease.gitplex.web.page.repository.info.RepositoryInfoPage;
import com.pmease.gitplex.web.page.repository.info.code.blob.RepoBlobPage;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;
import com.pmease.gitplex.web.page.repository.info.code.commits.RepoCommitsPage;

@SuppressWarnings("serial")
public class RepoTreePanel extends RepoInfoPanel {
	
	private final IModel<Commit> lastCommitModel;
	private final IModel<List<TreeNode>> nodesModel;
	
	public RepoTreePanel(String id, IModel<List<TreeNode>> nodesModel) {
		super(id);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getRepository().git();
				List<Commit> commits = git.log(null, getRevision(), getObjPath(), 1, 0);
				Commit c = Iterables.getFirst(commits, null);
				if (c == null) {
					throw new EntityNotFoundException("Unable to get commit for revision " + getRevision());
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

		add(new CommitMessagePanel("message", lastCommitModel));
		add(new CommitMetaPanel("meta", lastCommitModel).setAuthorMode(AvatarMode.NAME_AND_AVATAR));
		
		BookmarkablePageLink<Void> historyLink = new BookmarkablePageLink<Void>(
				"history",
				RepoCommitsPage.class,
				RepoCommitsPage.paramsOf(getRepository(), getRevision(), getObjPath(), 0));
		
		add(historyLink);
		BookmarkablePageLink<Void> commitLink = new BookmarkablePageLink<Void>(
				"commitlink",
				RepoCommitPage.class,
				RepoCommitPage.paramsOf(getRepository(), getLastCommit().getHash(), null));
		add(commitLink);
		commitLink.add(new Label("sha", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.abbreviateSHA(getLastCommit().getHash());
			}
		}));
		
		List<String> pathSegments = getObjPathSegments();
		if (pathSegments.isEmpty()) {
			add(new WebMarkupContainer("parent").setVisibilityAllowed(false));
		} else {
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("parent",
					RepoTreePage.class,
					RepoTreePage.paramsOf(getRepository(), getRevision(), Joiner.on("/").join(pathSegments.subList(0, pathSegments.size() - 1))));
			
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
							Git git = getRepository().git();
							if (git.isTreeLink(path, getRevision()))
								return "icon-folder-symlink";
							else
								return "icon-file-symlink";
						} else 
							return "icon-file-general";
					}
				});
				
				item.add(icon);
				
				List<String> pathSegments = Lists.newArrayList(getObjPathSegments());
				pathSegments.add(node.getName());
				
				PageParameters params = RepositoryInfoPage.paramsOf(getRepository(), getRevision(), 
						Joiner.on("/").join(pathSegments));
				
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
		if (lastCommitModel != null) {
			lastCommitModel.detach();
		}
		
		if (nodesModel != null) {
			nodesModel.detach();
		}
		
		super.onDetach();
	}
}

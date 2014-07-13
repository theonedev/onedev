package com.pmease.gitop.web.page.repository.source.tree;

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
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.commit.CommitMessagePanel;
import com.pmease.gitop.web.component.commit.CommitMetaPanel;
import com.pmease.gitop.web.component.user.AvatarMode;
import com.pmease.gitop.web.git.GitUtils;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.repository.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.repository.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.repository.source.commits.CommitsPage;
import com.pmease.gitop.web.page.repository.source.component.AbstractSourcePagePanel;

@SuppressWarnings("serial")
public class SourceTreePanel extends AbstractSourcePagePanel {
	
	private final IModel<Commit> lastCommitModel;
	private final IModel<List<TreeNode>> nodesModel;
	
	public SourceTreePanel(String id, 
			IModel<Repository> repo,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel,
			IModel<List<TreeNode>> nodesModel) {
		super(id, repo, revisionModel, pathsModel);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getRepo().git();
				List<String> paths = getPaths();
				List<Commit> commits = git.log(null, getRevision(), Joiner.on("/").join(paths), 1, 0);
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

		add(new CommitMessagePanel("message", lastCommitModel, repoModel));
		add(new CommitMetaPanel("meta", lastCommitModel).setAuthorMode(AvatarMode.NAME_AND_AVATAR));
		
		BookmarkablePageLink<Void> historyLink = new BookmarkablePageLink<Void>(
				"history",
				CommitsPage.class,
				CommitsPage.newParams(getRepo(), getRevision(), getPaths(), 0));
		
		add(historyLink);
		BookmarkablePageLink<Void> commitLink = new BookmarkablePageLink<Void>(
				"commitlink",
				SourceCommitPage.class,
				SourceCommitPage.newParams(getRepo(), getLastCommit().getHash()));
		add(commitLink);
		commitLink.add(new Label("sha", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.abbreviateSHA(getLastCommit().getHash());
			}
		}));
		
		List<String> paths = getPaths();
		if (paths.isEmpty()) {
			add(new WebMarkupContainer("parent").setVisibilityAllowed(false));
		} else {
			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("parent",
					SourceTreePage.class,
					SourceTreePage.newParams(getRepo(), getRevision(), paths.subList(0, paths.size() - 1)));
			
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
							Git git = getRepo().git();
							if (git.isTreeLink(path, getRevision()))
								return "icon-folder-symlink";
							else
								return "icon-file-symlink";
						} else 
							return "icon-file-general";
					}
				});
				
				item.add(icon);
				
				PageParameters params = new PageParameters();
				params.add(PageSpec.USER, getRepo().getOwner().getName());
				params.add(PageSpec.REPO, getRepo().getName());
				params.add(PageSpec.OBJECT_ID, getRevision());
				
				List<String> paths = Lists.newArrayList(getPaths());
				paths.add(node.getName());
				
				for (int i = 0; i < paths.size(); i++) {
					params.set(i, paths.get(i));
				}
				
				AbstractLink link;
				FileMode mode = node.getMode();
				if (mode == FileMode.TREE) {
					link = new BookmarkablePageLink<Void>("file", SourceTreePage.class, params);
				} else {
					link = new BookmarkablePageLink<Void>("file", SourceBlobPage.class, params);					
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
		response.render(OnDomReadyHeaderItem.forScript("gitop.commitMessage.toggle('.title-message .detailed-toggle');"));
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

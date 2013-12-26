package com.pmease.gitop.web.page.project.source.tree;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

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
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.git.UserInfo;
import com.pmease.commons.wicket.behavior.collapse.CollapseBehavior;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Icon;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.source.blob.SourceBlobPage;
import com.pmease.gitop.web.page.project.source.commit.SourceCommitPage;
import com.pmease.gitop.web.page.project.source.component.AbstractSourcePagePanel;
import com.pmease.gitop.web.util.GitUtils;

@SuppressWarnings("serial")
public class SourceTreePanel extends AbstractSourcePagePanel {
	
	private final IModel<Commit> lastCommitModel;
	private final IModel<List<TreeNode>> nodesModel;
	
	public SourceTreePanel(String id, 
			IModel<Project> project,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel,
			IModel<List<TreeNode>> nodesModel) {
		super(id, project, revisionModel, pathsModel);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getProject().code();
				List<String> paths = getPaths();
				List<Commit> commits = git.log(null, getRevision(), Joiner.on("/").join(paths), 1);
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
		
		add(new Label("shortMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getSubject();
			}
		}));
		
		Label detailedMsg = new Label("detailedMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getLastCommit().getMessage();
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (Objects.equal(getLastCommit().getSubject(), getLastCommit().getMessage())) {
					this.setVisibilityAllowed(false);
				}
			}
		};
		
		add(detailedMsg);
		
		WebMarkupContainer detailedToggle = new WebMarkupContainer("detailed-toggle");
		detailedToggle.add(new CollapseBehavior(detailedMsg));
		add(detailedToggle);
		
		add(new GitPersonLink("author", new AbstractReadOnlyModel<GitPerson>() {
			@Override
			public GitPerson getObject() {
				return GitPerson.of(getLastCommit().getAuthor());
			}
		},  Mode.FULL));

		add(new AgeLabel("author-date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getLastCommit().getAuthor().getDate();
			}
			
		}));
		
		add(new GitPersonLink("committer", new AbstractReadOnlyModel<GitPerson>() {

			@Override
			public GitPerson getObject() {
				return GitPerson.of(getLastCommit().getCommitter());
			}
		},  Mode.NAME_ONLY) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				Commit commit = getLastCommit();
				UserInfo author = commit.getAuthor();
				UserInfo committer = commit.getCommitter();
				this.setVisibilityAllowed(!Objects.equal(author, committer));
			}
		});
		
		add(new AgeLabel("committer-date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getLastCommit().getCommitter().getDate();
			}
			
		}));
		
		BookmarkablePageLink<Void> commitLink = new BookmarkablePageLink<Void>(
				"commitlink",
				SourceCommitPage.class,
				SourceCommitPage.newParams(getProject(), getLastCommit().getHash()));
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
					SourceTreePage.newParams(getProject(), getRevision(), paths.subList(0, paths.size() - 1)));
			
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
							Git git = getProject().code();
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
				params.add(PageSpec.USER, getProject().getOwner().getName());
				params.add(PageSpec.PROJECT, getProject().getName());
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
				
//				Git git = getProject().code();
//				Commit commit = GitUtils.getLastCommit(git, getRevision(), path);
//				item.add(new Label("message", Model.of(commit.getSubject())));
//				item.add(new AgeLabel("age", Model.of(commit.getAuthor().getDate())));
			}
		};
		
		add(fileTree);
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

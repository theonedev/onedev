package com.pmease.gitop.web.page.project.source.component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.web.common.bootstrap.Icon;
import com.pmease.gitop.web.component.link.CommitUserLink;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.source.SourceBlobPage;
import com.pmease.gitop.web.page.project.source.SourceCommitPage;
import com.pmease.gitop.web.page.project.source.SourceTreePage;
import com.pmease.gitop.web.util.DateUtils;
import com.pmease.gitop.web.util.GitUtils;
import com.pmease.gitop.web.util.UrlUtils;

@SuppressWarnings("serial")
public class SourceTreePanel extends AbstractSourcePagePanel {
	
	private final IModel<Commit> lastCommitModel;
	
	public SourceTreePanel(String id, 
			IModel<Project> project,
			IModel<String> revisionModel,
			IModel<List<String>> pathsModel) {
		super(id, project, revisionModel, pathsModel);
		
		lastCommitModel = new LoadableDetachableModel<Commit>() {

			@Override
			protected Commit load() {
				Git git = getProject().getCodeRepo();
				List<String> paths = getPaths();
				List<Commit> commits = git.log(null, getRevision(), Joiner.on("/").join(paths), 1);
				return Iterables.getFirst(commits, null);
			}
		};
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("shortMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.getCommitSummary(getLastCommit());
			}
		}));
		
		add(new CommitUserLink("author", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return getLastCommit().getAuthor();
			}
		}));

		add(new Label("author-date", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return DateUtils.formatAge(getLastCommit().getAuthorDate());
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
				return GitUtils.getShortSha(getLastCommit().getHash());
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
		
		IModel<List<TreeNode>> nodes = new LoadableDetachableModel<List<TreeNode>>() {

			@Override
			protected List<TreeNode> load() {
				Git git = getProject().getCodeRepo();
				List<String> paths = getPaths();
				String path = Joiner.on("/").join(paths);
				if (!Strings.isNullOrEmpty(path)) {
					path = UrlUtils.removeRedundantSlashes(path + "/");
				}
				
				List<TreeNode> nodes = Lists.newArrayList(git.listTree(getRevision(), path, false));
				
				Collections.sort(nodes, new Comparator<TreeNode>() {

					@Override
					public int compare(TreeNode o1, TreeNode o2) {
						if (o1.getType() == o2.getType()) {
							return o1.getName().compareTo(o2.getName());
						} else if (o1.getType() == TreeNode.Type.DIRECTORY) {
							return -1;
						} else if (o2.getType() == TreeNode.Type.DIRECTORY) {
							return 1;
						} else {
							return o1.getName().compareTo(o2.getName());
						}
					}
					
				});
				
				return nodes;
			}
		};
		
		ListView<TreeNode> fileTree = new ListView<TreeNode>("files", nodes) {

			@Override
			protected void populateItem(ListItem<TreeNode> item) {
				TreeNode node = item.getModelObject();
				final TreeNode.Type type = node.getType();
				Icon icon = new Icon("icon", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						switch (type) {
						case DIRECTORY:
							return "folder";
							
						case FILE:
							return "file";
							
						case SUBMODULE:
							return "submodule";
							
						case SYMBOLLINK:
							return "symbollink-folder";
						}
						
						return "";
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
				if (type == TreeNode.Type.DIRECTORY) {
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
	
	private Commit getLastCommit() {
		return lastCommitModel.getObject();
	}
	
	@Override
	public void onDetach() {
		if (lastCommitModel != null) {
			lastCommitModel.detach();
		}
		
		super.onDetach();
	}
}

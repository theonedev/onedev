package com.pmease.gitop.web.page.repository.source.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.page.repository.source.AbstractFilePage;
import com.pmease.gitop.web.page.repository.source.component.SourceBreadcrumbPanel;
import com.pmease.gitop.web.util.UrlUtils;

@SuppressWarnings("serial")
public class SourceTreePage extends AbstractFilePage {

	public static PageParameters newParams(Repository repository, String revision) {
		return newParams(repository, revision, Collections.<String>emptyList());
	}
	
	public static PageParameters newParams(Repository repository, String revision, List<String> paths) {
		PageParameters params = RepositoryPage.paramsOf(repository);
		params.add(RepositoryPage.PARAM_OBJECT_ID, revision);
		for (int i = 0; i < paths.size(); i++) {
			params.set(i, paths.get(i));
		}
		
		return params;
	}
	
	public SourceTreePage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<TreeNode>> nodesModel = new LoadableDetachableModel<List<TreeNode>>() {

			@Override
			protected List<TreeNode> load() {
				Git git = getRepository().git();
				List<String> paths = getPaths();
				String path = Joiner.on("/").join(paths);
				if (!Strings.isNullOrEmpty(path)) {
					path = UrlUtils.removeRedundantSlashes(path + "/");
				}
				
				List<TreeNode> nodes = Lists.newArrayList(git.listTree(getRevision(), path, false));
				
				Collections.sort(nodes, new Comparator<TreeNode>() {

					@Override
					public int compare(TreeNode o1, TreeNode o2) {
						if (o1.getMode() == o2.getMode()) {
							return o1.getName().compareTo(o2.getName());
						} else if (o1.getMode() == FileMode.TREE) {
							return -1;
						} else if (o2.getMode() == FileMode.TREE) {
							return 1;
						} else {
							return o1.getName().compareTo(o2.getName());
						}
					}
					
				});
				
				return nodes;
			}
		};
		
		if (getRepository().git().hasCommits()) {
			add(new RepositoryDescriptionPanel("description", repositoryModel).setVisibilityAllowed(getPaths().isEmpty()));
			add(new SourceBreadcrumbPanel("breadcrumb", repositoryModel, revisionModel, pathsModel));
			add(new SourceTreePanel("tree", repositoryModel, revisionModel, pathsModel, nodesModel));
			add(new ReadmePanel("readme", repositoryModel, revisionModel, pathsModel, nodesModel));
		} else {
			add(new WebMarkupContainer("tree").setVisibilityAllowed(false));
		}
	}
	
	@Override
	protected String getPageTitle() {
		List<String> paths = getPaths();
		String rev = getRevision();
		Repository repository = getRepository();
		
		if (paths.isEmpty()) {
			return repository.getFullName();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(Joiner.on("/").join(paths))
				.append(" at ").append(rev)
				.append(" - ").append(repository.getFullName());
			
			return sb.toString();
		}
	}

}

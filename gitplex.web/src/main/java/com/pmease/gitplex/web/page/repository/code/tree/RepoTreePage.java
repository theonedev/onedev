package com.pmease.gitplex.web.page.repository.code.tree;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.code.component.SourceBreadcrumbPanel;
import com.pmease.gitplex.web.util.UrlUtils;

@SuppressWarnings("serial")
public class RepoTreePage extends RepositoryPage {

	public RepoTreePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IModel<List<TreeNode>> nodesModel = new LoadableDetachableModel<List<TreeNode>>() {

			@Override
			protected List<TreeNode> load() {
				Git git = getRepository().git();
				String path = getCurrentPath();
				if (!Strings.isNullOrEmpty(path)) {
					path = UrlUtils.removeRedundantSlashes(path + "/");
				}
				
				List<TreeNode> nodes = Lists.newArrayList(git.listTree(getRepository().defaultBranchIfNull(getCurrentRevision()), path));
				
				Collections.sort(nodes, new Comparator<TreeNode>() {

					@Override
					public int compare(TreeNode o1, TreeNode o2) {
						if (o1.getMode() == o2.getMode()) {
							return o1.getName().compareTo(o2.getName());
						} else if (o1.getMode() == FileMode.TYPE_TREE) {
							return -1;
						} else if (o2.getMode() == FileMode.TYPE_TREE) {
							return 1;
						} else {
							return o1.getName().compareTo(o2.getName());
						}
					}
					
				});
				
				return nodes;
			}
		};
		
		add(new RepoDescribePanel("description", repoModel).setVisible(getCurrentPath() != null));
		add(new SourceBreadcrumbPanel("breadcrumb", repoModel, currentRevision, currentPath));
		add(new RepoTreePanel("tree", repoModel, currentRevision, currentPath, nodesModel));
		add(new ReadmePanel("readme", repoModel, currentRevision, nodesModel));
	}
	
	@Override
	protected String getPageTitle() {
		Repository repository = getRepository();
		
		if (getCurrentPath() == null) {
			return repository.getFQN();
		} else {
			StringBuffer sb = new StringBuffer();
			sb.append(getCurrentPath())
				.append(" at ").append(repository.defaultBranchIfNull(getCurrentRevision()))
				.append(" - ").append(repository.getFQN());
			
			return sb.toString();
		}
	}

}

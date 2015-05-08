package com.pmease.gitplex.web.page.repository.code.tree;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.pathnavigator.PathNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.component.treelist.TreeList;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoTreePage extends RepositoryPage {

	private String revision = "master";
	
	@Nullable
	private String path;
	
	private PathNavigator pathNavigator;
	
	private TreeList treeList;
	
	public RepoTreePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new RevisionSelector("revSelector", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return revision;
			}

			@Override
			public void setObject(String object) {
				revision = object;
			}
			
		}) {

			@Override
			protected Repository getRepository() {
				return repoModel.getObject();
			}

			@Override
			protected void onModelChanged() {
				super.onModelChanged();
				
				if (path != null) {
					org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
					try {
						ObjectId commitId = getRepository().resolveRevision(revision);
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, path, revTree);
						if (treeWalk == null)
							path = null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (target != null) {
					target.add(pathNavigator);
					target.add(treeList);
				}
			}

		});
		
		add(pathNavigator = new PathNavigator("pathSelector", repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return revision;
			}
			
		}, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return path;
			}

			@Override
			public void setObject(String object) {
				path = object;
			}
			
		}) {

			@Override
			protected void onModelChanged() {
				super.onModelChanged();
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (target != null)
					target.add(treeList);
			}
			
		});
		
		add(treeList = new TreeList("treeList", repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return revision;
			}
			
		}, new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return path;
			}

			@Override
			public void setObject(String object) {
				path = object;
			}
			
		}) {

			@Override
			protected void onModelChanged() {
				super.onModelChanged();
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (target != null)
					target.add(pathNavigator);
			}
			
		});
		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoTreePage.class, "repo-tree-page.css")));
	}

}

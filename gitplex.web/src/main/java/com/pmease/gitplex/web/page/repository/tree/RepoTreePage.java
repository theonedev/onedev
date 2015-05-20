package com.pmease.gitplex.web.page.repository.tree;

import java.io.IOException;
import java.io.Serializable;

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

import com.pmease.commons.git.GitPath;
import com.pmease.commons.wicket.behavior.HistoryBehavior;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.component.pathnavigator.PathNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.component.treelist.TreeList;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoTreePage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";

	private String revision = "master";
	
	private String path;
	
	private RevisionSelector revisionSelector;
	
	private PathNavigator pathNavigator;
	
	private TreeList treeList;
	
	private HistoryBehavior historyBehavior;
	
	public RepoTreePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		revision = GitPath.normalize(params.get(PARAM_REVISION).toString());
		path = GitPath.normalize(params.get(PARAM_PATH).toString());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(revisionSelector = new RevisionSelector("revSelector", new IModel<String>() {

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
					
					pushState(target);
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
				if (target != null) {
					target.add(treeList);
					pushState(target);
				}
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
				if (target != null) {
					target.add(pathNavigator);
					pushState(target);
				}
			}
			
		});
		
		add(historyBehavior = new HistoryBehavior() {

			@Override
			protected void onPopState(AjaxRequestTarget target, Serializable state) {
				HistoryState historyState = (HistoryState) state;
				revision = historyState.revision;
				path = historyState.path;
				
				target.add(revisionSelector);
				target.add(pathNavigator);
				target.add(treeList);
			}
			
		});
	}
	
	private void pushState(AjaxRequestTarget target) {
		HistoryState state = new HistoryState();
		state.revision = revision;
		state.path = path;
		PageParameters params = paramsOf(getRepository(), revision, path);
		String url = RequestCycle.get().urlFor(RepoTreePage.class, params).toString();
		historyBehavior.pushState(target, url, state);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoTreePage.class, "repo-tree.css")));
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}
	
	private static class HistoryState implements Serializable {
		String revision;
		
		String path;
	}
}

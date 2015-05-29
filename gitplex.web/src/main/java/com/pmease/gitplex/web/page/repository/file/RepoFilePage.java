package com.pmease.gitplex.web.page.repository.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitPath;
import com.pmease.commons.wicket.behavior.HistoryBehavior;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.filelist.FileListPanel;
import com.pmease.gitplex.web.component.filenavigator.FileNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.component.sourceview.Source;
import com.pmease.gitplex.web.component.sourceview.SourceViewPanel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoFilePage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";

	private static final String REVISION_SELECTOR_ID = "revisionSelector";
	
	private static final String FILE_NAVIGATOR_ID = "fileNavigator";
	
	private static final String FILE_VIEWER_ID = "fileViewer";

	private String revision;
	
	@Nullable
	private GitPath file;
	
	private Component revisionSelector;
	
	private Component fileNavigator;
	
	private Component fileViewer;
	
	private HistoryBehavior historyBehavior;
	
	public RepoFilePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		revision = GitPath.normalize(params.get(PARAM_REVISION).toString());
		if (revision == null)
			revision = getRepository().getDefaultBranch().getName();
		
		String pathName = GitPath.normalize(params.get(PARAM_PATH).toString());
		if (pathName != null) {
			org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
			try {
				ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(revision));
				RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
				TreeWalk treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, pathName, revTree));
				file = new GitPath(pathName, treeWalk.getRawMode(0));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				jgitRepo.close();
			}
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		newRevisionSelector(null);
		newFileNavigator(null);
		newFileViewer(null);
		
		add(historyBehavior = new HistoryBehavior() {

			@Override
			protected void onPopState(AjaxRequestTarget target, Serializable state) {
				HistoryState historyState = (HistoryState) state;
				revision = historyState.revision;
				file = historyState.path;

				newRevisionSelector(target);
				newFileNavigator(target);
				newFileViewer(target);
			}
			
		});
	}

	private void newRevisionSelector(AjaxRequestTarget target) {
		revisionSelector = new RevisionSelector(REVISION_SELECTOR_ID, repoModel, revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				RepoFilePage.this.revision = revision;
				if (file != null) {
					org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
					try {
						ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(revision));
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, file.getName(), revTree);
						if (treeWalk == null)
							file = null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}

				newRevisionSelector(target);
				newFileNavigator(target);
				newFileViewer(target);
				
				pushState(target);
			}

		};
		
		if (target != null) {
			replace(revisionSelector);
			target.add(revisionSelector);
		} else {
			add(revisionSelector);
		}
	}
	
	private void newFileNavigator(AjaxRequestTarget target) {
		fileNavigator = new FileNavigator(FILE_NAVIGATOR_ID, repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return revision;
			}
			
		}, file) {

			@Override
			protected void onSelect(AjaxRequestTarget target, GitPath file) {
				RepoFilePage.this.file = file;

				newFileNavigator(target);
				newFileViewer(target);
				
				pushState(target);
			}
			
		};
		if (target != null) {
			replace(fileNavigator);
			target.add(fileNavigator);
		} else {
			add(fileNavigator);
		}
	}
	
	private void newFileViewer(AjaxRequestTarget target) {
		if (file == null || file.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, repoModel, new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return revision;
				}
				
			}, file) {

				@Override
				protected void onSelect(AjaxRequestTarget target, GitPath file) {
					RepoFilePage.this.file = file;
					
					newFileViewer(target);
					newFileNavigator(target);
					
					pushState(target);
				}
				
			};
		} else {
			org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
			try {
				ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(revision));
				RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
				TreeWalk treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, file.getName(), revTree));
				ObjectLoader objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				String content = new String(objectLoader.getCachedBytes());
				Source source = new Source(revision, file.getName(), content, 0);
				fileViewer = new SourceViewPanel(FILE_VIEWER_ID, repoModel, source) {

					@Override
					protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
						file = new GitPath(hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
						
						newFileNavigator(target);
						newFileViewer(target);
						
						pushState(target);
					}

					@Override
					protected void onCompleteOccurrencesSearch(AjaxRequestTarget target, List<QueryHit> hits) {
						
					}
					
				};
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				jgitRepo.close();
			}
		}
		
		if (target != null) {
			replace(fileViewer);
			target.add(fileViewer);
			target.appendJavaScript("$(window).resize();");
		} else {
			add(fileViewer);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		HistoryState state = new HistoryState();
		state.revision = revision;
		state.path = file;
		PageParameters params = paramsOf(getRepository(), revision, file!=null?file.getName():null);
		String url = RequestCycle.get().urlFor(RepoFilePage.class, params).toString();
		historyBehavior.pushState(target, url, state);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(RepoFilePage.class, "repo-file.js")));
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoFilePage.class, "repo-file.css")));
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
		
		GitPath path;
	}
}

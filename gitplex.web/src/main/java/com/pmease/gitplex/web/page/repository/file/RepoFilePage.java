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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.GitPath;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.HistoryBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.search.IndexManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.blobsearch.BlobSearchPanel;
import com.pmease.gitplex.web.component.filelist.FileListPanel;
import com.pmease.gitplex.web.component.filenavigator.FileNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.component.sourceview.Source;
import com.pmease.gitplex.web.component.sourceview.SourceViewPanel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIResizableJavaScriptReference;

@SuppressWarnings("serial")
public class RepoFilePage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";

	private static final String REVISION_SELECTOR_ID = "revisionSelector";
	
	private static final String FILE_NAVIGATOR_ID = "fileNavigator";
	
	private static final String FILE_VIEWER_ID = "fileViewer";
	
	private static final String SEARCH_RESULD_ID = "searchResult";

	private State state = new State();
	
	private Component revisionSelector;
	
	private Component fileNavigator;
	
	private Component revisionIndexing;
	
	private Component fileViewer;
	
	private HistoryBehavior historyBehavior;
	
	private final RevisionIndexed trait = new RevisionIndexed();
	
	public RepoFilePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		trait.repoId = getRepository().getId();
		state.revision = GitPath.normalize(params.get(PARAM_REVISION).toString());
		if (state.revision == null)
			state.revision = getRepository().getDefaultBranch().getName();
		trait.revision = state.revision;
		
		String pathName = GitPath.normalize(params.get(PARAM_PATH).toString());
		if (pathName != null) {
			org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
			try {
				ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(state.revision));
				RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
				TreeWalk treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, pathName, revTree));
				state.file = new GitPath(pathName, treeWalk.getRawMode(0));
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
		
		add(new BlobSearchPanel("fileSearcher", repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return state.revision;
			}
			
		}) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				state.file = new GitPath(hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
				state.line = hit.getLineNo();
				
				newFileNavigator(target);
				newFileViewer(target);
				
				pushState(target);
			}
			
			@Override
			protected void renderQueryHits(AjaxRequestTarget target, List<QueryHit> hits) {
				renderSearchResult(target, hits);
			}
			
		});
		
		revisionIndexing = new WebMarkupContainer("revisionIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new Image("icon", new PackageResourceReference(RepoFilePage.class, "indexing.gif")));
				
				setOutputMarkupPlaceholderTag(true);
			}

		};
		
		add(revisionIndexing = new WebMarkupContainer("revisionIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Image("icon", new PackageResourceReference(RepoFilePage.class, "indexing.gif")));
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				if (!indexManager.isIndexed(getRepository(), state.revision)) {
					GitPlex.getInstance(UnitOfWork.class).asyncCall(new Runnable() {

						@Override
						public void run() {
							GitPlex.getInstance(IndexManager.class).index(getRepository(), state.revision);
						}
						
					});
					setVisible(true);
				} else {
					setVisible(false);
				}
			}
			
		});

		add(new WebSocketRenderBehavior() {
			
			@Override
			protected Object getTrait() {
				return trait;
			}

			@Override
			protected void onRender(WebSocketRequestHandler handler) {
				handler.add(revisionIndexing);
				handler.appendJavaScript("$(window).resize();");
			}
			
		});
		
		add(new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true));
		
		add(historyBehavior = new HistoryBehavior() {

			@Override
			protected void onPopState(AjaxRequestTarget target, Serializable state) {
				RepoFilePage.this.state = (State) state;
				trait.revision = RepoFilePage.this.state.revision;

				target.add(revisionIndexing);
				newRevisionSelector(target);
				newFileNavigator(target);
				newFileViewer(target);
			}
			
		});
	}
	
	private void newRevisionSelector(AjaxRequestTarget target) {
		revisionSelector = new RevisionSelector(REVISION_SELECTOR_ID, repoModel, state.revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				state.revision = revision;
				trait.revision = revision;
				if (state.file != null) {
					org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
					try {
						ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(revision));
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, state.file.getName(), revTree);
						if (treeWalk == null)
							state.file = null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}
				state.line = 0;

				target.add(revisionIndexing);
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
				return state.revision;
			}
			
		}, state.file) {

			@Override
			protected void onSelect(AjaxRequestTarget target, GitPath file) {
				state.file = file;
				state.line = 0;

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
		if (state.file == null || state.file.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, repoModel, new AbstractReadOnlyModel<String>() {

				@Override
				public String getObject() {
					return state.revision;
				}
				
			}, state.file) {

				@Override
				protected void onSelect(AjaxRequestTarget target, GitPath file) {
					state.file = file;
					state.line = 0;
					
					newFileViewer(target);
					newFileNavigator(target);
					
					pushState(target);
				}
				
			};
		} else {
			org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
			try {
				ObjectId commitId = Preconditions.checkNotNull(getRepository().resolveRevision(state.revision));
				RevTree revTree = new RevWalk(jgitRepo).parseCommit(commitId).getTree();
				TreeWalk treeWalk = Preconditions.checkNotNull(TreeWalk.forPath(jgitRepo, state.file.getName(), revTree));
				ObjectLoader objectLoader = treeWalk.getObjectReader().open(treeWalk.getObjectId(0));
				String content = new String(objectLoader.getCachedBytes());
				Source source = new Source(state.revision, state.file.getName(), content, state.line);
				fileViewer = new SourceViewPanel(FILE_VIEWER_ID, repoModel, source) {

					@Override
					protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
						state.file = new GitPath(hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
						state.line = hit.getLineNo();
						
						newFileNavigator(target);
						newFileViewer(target);
						
						pushState(target);
					}

					@Override
					protected void renderQueryHits(AjaxRequestTarget target, List<QueryHit> hits) {
						renderSearchResult(target, hits);
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
		PageParameters params = paramsOf(getRepository(), state.revision, state.file!=null?state.file.getName():null);
		String url = RequestCycle.get().urlFor(RepoFilePage.class, params).toString();
		historyBehavior.pushState(target, url, state);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JQueryUIResizableJavaScriptReference.asHeaderItem());
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RepoFilePage.class, "repo-file.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RepoFilePage.class, "repo-file.css")));
	}

	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		return params;
	}
	
	private void renderSearchResult(AjaxRequestTarget target, List<QueryHit> hits) {
		Component searchResult = new SearchResultPanel(SEARCH_RESULD_ID, hits) {
				
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				state.file = new GitPath(hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
				state.line = hit.getLineNo();
				
				newFileNavigator(target);
				newFileViewer(target);

				pushState(target);
			}

			@Override
			protected void onClose(AjaxRequestTarget target) {
				WebMarkupContainer searchResult = new WebMarkupContainer(SEARCH_RESULD_ID);
				searchResult.setOutputMarkupId(true);
				getPage().replace(searchResult);
				target.add(searchResult);
				target.appendJavaScript("$('#repo-file>.search-result').hide(); $(window).resize();");
			}
			
		};
		replace(searchResult);
		target.add(searchResult);
		target.appendJavaScript("$('#repo-file>.search-result').show(); $(window).resize();");
	}
	
	private static class RevisionIndexed implements Serializable {

		Long repoId;
		
		volatile String revision;
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof RevisionIndexed))  
				return false;  
			RevisionIndexed other = (RevisionIndexed) obj;  
		    return Objects.equal(repoId, other.repoId) && Objects.equal(revision, other.revision);
		}
		
	}
	
	private static class State implements Serializable {

		String revision;
		
		@Nullable
		GitPath file;
		
		int line;
	}
	
	public static class IndexedListener implements IndexListener {

		@Override
		public void commitIndexed(Repository repository, String revision) {
			RevisionIndexed trait = new RevisionIndexed();
			trait.repoId = repository.getId();
			trait.revision = revision;
			WebSocketRenderBehavior.requestToRender(trait);
		}

		@Override
		public void indexRemoving(Repository repository) {
		}
		
	}
}

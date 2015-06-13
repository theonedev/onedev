package com.pmease.gitplex.web.page.repository.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Objects;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.ObjectNotFoundException;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.lang.TokenPosition;
import com.pmease.commons.wicket.assets.closestdescendant.ClosestDescendantResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.HistoryBehavior;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.search.IndexManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.blobsearch.BlobSearchPanel;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.source.SourceViewPanel;
import com.pmease.gitplex.web.component.filelist.FileListPanel;
import com.pmease.gitplex.web.component.filenavigator.FileNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIResizableJavaScriptReference;

@SuppressWarnings("serial")
public class RepoFilePage extends RepositoryPage {

	public static final int MAX_QUERY_ENTRIES = 1000;
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";

	private static final String REVISION_SELECTOR_ID = "revisionSelector";
	
	private static final String FILE_NAVIGATOR_ID = "fileNavigator";
	
	private static final String LAST_COMMIT_ID = "lastCommit";
	
	private static final String FILE_VIEWER_ID = "fileViewer";
	
	private static final String SEARCH_RESULD_ID = "searchResult";

	private BlobIdent file = new BlobIdent();
	
	private TokenPosition tokenPos;
	
	private Component revisionSelector;
	
	private Component fileNavigator;
	
	private Component revisionIndexing;
	
	private Component lastCommit;
	
	private Component fileViewer;
	
	private HistoryBehavior historyBehavior;
	
	private final RevisionIndexed trait = new RevisionIndexed();
	
	public RepoFilePage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		trait.repoId = getRepository().getId();
		file.revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
		if (file.revision == null)
			file.revision = getRepository().getDefaultBranch().getName();
		trait.revision = file.revision;
		
		file.path = GitUtils.normalizePath(params.get(PARAM_PATH).toString());
		if (file.path != null) {
			org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
			try {
				RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, file.path, revTree);
				if (treeWalk == null) {
					throw new ObjectNotFoundException("Unable to find blob path '" + file.path
							+ "' in revision '" + file.revision + "'");
				}
				file.mode = treeWalk.getRawMode(0);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				jgitRepo.close();
			}
		}
	}
	
	private ObjectId getCommitId() {
		return getRepository().getObjectId(file.revision, true);
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
				return file.revision;
			}
			
		}) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				file = new BlobIdent(file.revision, hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
				tokenPos = hit.getTokenPos();
				
				newFileNavigator(target);
				newFileViewer(target);
				
				pushState(target);
			}
			
			@Override
			protected void renderQueryHits(AjaxRequestTarget target, List<QueryHit> hits) {
				renderSearchResult(target, hits);
			}
			
		});
		
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
				if (!indexManager.isIndexed(getRepository(), file.revision)) {
					GitPlex.getInstance(UnitOfWork.class).asyncCall(new Runnable() {

						@Override
						public void run() {
							GitPlex.getInstance(IndexManager.class).index(getRepository(), file.revision);
						}
						
					});
					setVisible(true);
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true));

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
		
		add(historyBehavior = new HistoryBehavior() {

			@Override
			protected void onPopState(AjaxRequestTarget target, Serializable state) {
				file = ((State) state).file;
				tokenPos = ((State) state).tokenPos;
				trait.revision = file.revision;

				target.add(revisionIndexing);
				newRevisionSelector(target);
				newFileNavigator(target);
				newFileViewer(target);
			}
			
		});
	}
	
	private void newRevisionSelector(AjaxRequestTarget target) {
		revisionSelector = new RevisionSelector(REVISION_SELECTOR_ID, repoModel, file.revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				file.revision = revision;
				trait.revision = revision;
				if (file.path != null) {
					org.eclipse.jgit.lib.Repository jgitRepo = getRepository().openAsJGitRepo();
					try {
						RevTree revTree = new RevWalk(jgitRepo).parseCommit(getCommitId()).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, file.path, revTree);
						if (treeWalk == null)
							file.path = null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						jgitRepo.close();
					}
				}
				tokenPos = null;

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
		fileNavigator = new FileNavigator(FILE_NAVIGATOR_ID, repoModel, file) {

			@Override
			protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
				RepoFilePage.this.file = file;
				tokenPos = null;

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
		if (target != null && fileViewer instanceof SourceViewPanel) {
			SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
			if (sourceViewer.getContext().getBlobIdent().equals(file)) {
				if (tokenPos != null)
					sourceViewer.highlightToken(target, tokenPos);
				return;
			}
		}
		
		if (file.path == null || file.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, repoModel, file) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
					RepoFilePage.this.file = file;
					tokenPos = null;
					
					newFileViewer(target);
					newFileNavigator(target);
					
					pushState(target);
				}
				
			};
		} else {
			fileViewer = new BlobViewContext(new BlobIdent(file)) {

				@Override
				public Repository getRepository() {
					return RepoFilePage.this.getRepository();
				}

				@Override
				public TokenPosition getTokenPosition() {
					return tokenPos;
				}

				@Override
				public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, TokenPosition tokenPos) {
					RepoFilePage.this.file = new BlobIdent(file.revision, blobIdent.path, 
							FileMode.REGULAR_FILE.getBits()); 
					RepoFilePage.this.tokenPos = tokenPos;
					
					newFileNavigator(target);
					newFileViewer(target);
					
					pushState(target);
				}

				@Override
				public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
					renderSearchResult(target, hits);
				}
				
			}.render(FILE_VIEWER_ID);
		}
		
		lastCommit = new AjaxLazyLoadPanel(LAST_COMMIT_ID) {
			
			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
				String html = "<img src='" + RequestCycle.get().urlFor(handler) + "' class='loading'/> Loading latest commit...";
				return new Label(markupId, html).setEscapeModelStrings(false);
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				return new LastCommitPanel(markupId, repoModel, file);
			}
		};
		
		if (target != null) {
			replace(fileViewer);
			target.add(fileViewer);
			replace(lastCommit);
			target.add(lastCommit);
			target.appendJavaScript("$(window).resize();");
		} else {
			add(fileViewer);
			add(lastCommit);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getRepository(), file.revision, file.path);
		String url = RequestCycle.get().urlFor(RepoFilePage.class, params).toString();
		State state = new State();
		state.file = file;
		state.tokenPos = tokenPos;
		historyBehavior.pushState(target, url, state);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(ClosestDescendantResourceReference.INSTANCE));
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
		Component searchResult = newSearchResult(hits);
		
		replace(searchResult);
		target.add(searchResult);
		target.appendJavaScript(""
				+ "$('#repo-file>.search-result').show(); "
				+ "$('#repo-file>.search-result>div>.body').focus(); "
				+ "$(window).resize();");
	}
	
	private Component newSearchResult(List<QueryHit> hits) {
		return new SearchResultPanel(SEARCH_RESULD_ID, hits) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				file = new BlobIdent(file.revision, hit.getBlobPath(), FileMode.REGULAR_FILE.getBits()); 
				tokenPos = hit.getTokenPos();
				
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

		BlobIdent file = new BlobIdent();
		
		TokenPosition tokenPos;
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

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
import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.search.IndexManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.blobsearch.advanced.AdvancedSearchPanel;
import com.pmease.gitplex.web.component.blobsearch.instant.InstantSearchPanel;
import com.pmease.gitplex.web.component.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.blobview.source.SourceViewPanel;
import com.pmease.gitplex.web.component.filelist.FileListPanel;
import com.pmease.gitplex.web.component.filenavigator.FileNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIResizableJavaScriptReference;

@SuppressWarnings("serial")
public class RepoFilePage extends RepositoryPage {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_BLAME = "blame";
	
	private static final String REVISION_SELECTOR_ID = "revisionSelector";
	
	private static final String FILE_NAVIGATOR_ID = "fileNavigator";
	
	private static final String LAST_COMMIT_ID = "lastCommit";
	
	private static final String FILE_VIEWER_ID = "fileViewer";
	
	private static final String SEARCH_RESULD_ID = "searchResult";

	private BlobIdent file = new BlobIdent();
	
	private TokenPosition tokenPos;
	
	private boolean blame;
	
	private Component revisionSelector;
	
	private Component fileNavigator;
	
	private Component revisionIndexing;
	
	private Component lastCommit;
	
	private Component fileViewer;
	
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
		
		blame = params.get(PARAM_BLAME).toBoolean(false);
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
		
		add(new InstantSearchPanel("instantSearch", repoModel, new AbstractReadOnlyModel<String>() {

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
			protected void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
				renderSearchResult(target, hits);
			}
			
		});
		
		ModalPanel advancedSearchModal = new ModalPanel("advancedSearchModal") {

			@Override
			protected Component newContent(String id, ModalBehavior behavior) {
				return new AdvancedSearchPanel(id, repoModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return file.revision;
					}
					
				}) {

					@Override
					protected void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
						renderSearchResult(target, hits);
						close(target);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						close(target);
					}

					@Override
					protected BlobIdent getCurrentBlob() {
						return file;
					}
					
				};
			}
			
		};
		add(advancedSearchModal);
		add(new WebMarkupContainer("advancedSearch").add(new ModalBehavior(advancedSearchModal)));
		
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
				blame = false;

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
				blame = false;

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
			BlobViewContext context = sourceViewer.getContext();
			if (context.getBlobIdent().equals(file) && context.isBlame() == blame) {
				sourceViewer.getContext().setTokenPosition(tokenPos);
				if (tokenPos != null) {
					sourceViewer.highlightToken(target, tokenPos);
				} else {
					BlobViewPanel blobViewer = sourceViewer.getContext().render(FILE_VIEWER_ID);
					if (!(blobViewer instanceof SourceViewPanel)) {
						fileViewer.replaceWith(blobViewer);
						fileViewer = blobViewer;
						target.add(fileViewer);
						target.appendJavaScript("$(window).resize();");
					}
				}
				return;
			}
		}
		
		if (file.path == null || file.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, repoModel, file) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
					RepoFilePage.this.file = file;
					tokenPos = null;
					blame = false;
					
					newFileViewer(target);
					newFileNavigator(target);
					
					pushState(target);
				}
				
			};
		} else {
			BlobViewContext context = new BlobViewContext(new BlobIdent(file)) {

				@Override
				public Repository getRepository() {
					return RepoFilePage.this.getRepository();
				}

				@Override
				public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, TokenPosition tokenPos) {
					RepoFilePage.this.file = new BlobIdent(file.revision, blobIdent.path, 
							FileMode.REGULAR_FILE.getBits()); 
					RepoFilePage.this.tokenPos = tokenPos;
					blame = false;
					
					newFileNavigator(target);
					newFileViewer(target);
					
					pushState(target);
				}

				@Override
				public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
					renderSearchResult(target, hits);
				}

				@Override
				public void onBlameChange(AjaxRequestTarget target) {
					blame = !blame;
					
					newFileViewer(target);
					
					pushState(target);
				}
				
			};
			context.setTokenPosition(tokenPos);
			context.setBlame(blame);
			
			fileViewer = context.render(FILE_VIEWER_ID);
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
		PageParameters params = paramsOf(getRepository(), file.revision, file.path, blame);
		CharSequence url = RequestCycle.get().urlFor(RepoFilePage.class, params);
		HistoryData state = new HistoryData();
		state.file = file;
		state.tokenPos = tokenPos;
		state.blame = blame;
		pushState(target, url.toString(), state);
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
		return paramsOf(repository, revision, path, false);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path, boolean blame) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		if (blame)
			params.set(PARAM_BLAME, blame);
		return params;
	}
	
	public static PageParameters paramsOf(Repository repository, BlobIdent blobIdent) {
		return paramsOf(repository, blobIdent, false);
	}
	
	public static PageParameters paramsOf(Repository repository, BlobIdent blobIdent, boolean blame) {
		return paramsOf(repository, blobIdent.revision, blobIdent.path, blame);
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
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		HistoryData historyData = (HistoryData) data;
		file = historyData.file;
		tokenPos = historyData.tokenPos;
		blame = historyData.blame;
		
		trait.revision = file.revision;

		target.add(revisionIndexing);
		newRevisionSelector(target);
		newFileNavigator(target);
		newFileViewer(target);
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
	
	private static class HistoryData implements Serializable {

		BlobIdent file = new BlobIdent();
		
		TokenPosition tokenPos;
		
		boolean blame;
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

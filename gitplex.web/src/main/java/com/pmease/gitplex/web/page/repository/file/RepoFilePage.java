package com.pmease.gitplex.web.page.repository.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Objects;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.exception.ObjectNotExistException;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.commons.wicket.assets.closestdescendant.ClosestDescendantResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalBehavior;
import com.pmease.commons.wicket.behavior.modal.ModalPanel;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketTrait;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.listeners.RepositoryListener;
import com.pmease.gitplex.core.model.Comment;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.search.IndexManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.web.component.repofile.blobsearch.advanced.AdvancedSearchPanel;
import com.pmease.gitplex.web.component.repofile.blobsearch.instant.InstantSearchPanel;
import com.pmease.gitplex.web.component.repofile.blobsearch.result.SearchResultPanel;
import com.pmease.gitplex.web.component.repofile.blobview.BlobNameChangeCallback;
import com.pmease.gitplex.web.component.repofile.blobview.BlobRenderer;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewContext;
import com.pmease.gitplex.web.component.repofile.blobview.BlobViewPanel;
import com.pmease.gitplex.web.component.repofile.blobview.source.SourceViewPanel;
import com.pmease.gitplex.web.component.repofile.editsave.CancelListener;
import com.pmease.gitplex.web.component.repofile.editsave.EditSavePanel;
import com.pmease.gitplex.web.component.repofile.fileedit.FileEditPanel;
import com.pmease.gitplex.web.component.repofile.filelist.FileListPanel;
import com.pmease.gitplex.web.component.repofile.filenavigator.FileNavigator;
import com.pmease.gitplex.web.component.revisionselector.RevisionSelector;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.websocket.PullRequestChangeRenderer;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIResizableJavaScriptReference;

@SuppressWarnings("serial")
public class RepoFilePage extends RepositoryPage implements BlobViewContext {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_BLAME = "blame";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String REVISION_SELECTOR_ID = "revisionSelector";
	
	private static final String FILE_NAVIGATOR_ID = "fileNavigator";
	
	private static final String LAST_COMMIT_ID = "lastCommit";
	
	private static final String FILE_VIEWER_ID = "fileViewer";
	
	private static final String SEARCH_RESULD_ID = "searchResult";

	private Long requestId;
	
	private Long commentId;
	
	private final IModel<Comment> commentModel = new LoadableDetachableModel<Comment>() {

		@Override
		protected Comment load() {
			if (commentId != null)
				return GitPlex.getInstance(Dao.class).load(Comment.class, commentId);
			else
				return null;
		}
		
	};
	
	private final IModel<PullRequest> requestModel = new LoadableDetachableModel<PullRequest>() {

		@Override
		protected PullRequest load() {
			Comment comment = getComment();
			if (comment != null)
				return comment.getRequest();
			else if (requestId != null)
				return GitPlex.getInstance(Dao.class).load(PullRequest.class, requestId);
			else
				return null;
		}
	};
	
	private BlobIdent blobIdent = new BlobIdent();
	
	private TokenPosition tokenPos;
	
	public boolean blame;
	
	private Component revisionSelector;
	
	private Component fileNavigator;
	
	private Component revisionIndexing;
	
	private Component lastCommit;
	
	private Component fileViewer;
	
	private final RevisionIndexed trait = new RevisionIndexed();
	
	public RepoFilePage(final PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
		
		trait.repoId = getRepository().getId();
		blobIdent.revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
		if (blobIdent.revision == null)
			blobIdent.revision = getRepository().getDefaultBranch();
		trait.revision = blobIdent.revision;
		
		blobIdent.path = GitUtils.normalizePath(params.get(PARAM_PATH).toString());
		if (blobIdent.path != null) {
			try (	FileRepository jgitRepo = getRepository().openAsJGitRepo();
					RevWalk revWalk = new RevWalk(jgitRepo)) {
				RevTree revTree = revWalk.parseCommit(getCommitId()).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobIdent.path, revTree);
				if (treeWalk == null) {
					throw new ObjectNotExistException("Unable to find blob path '" + blobIdent.path
							+ "' in revision '" + blobIdent.revision + "'");
				}
				blobIdent.mode = treeWalk.getRawMode(0);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			blobIdent.mode = FileMode.TREE.getBits();
		}
		
		blame = params.get(PARAM_BLAME).toBoolean(false);
		commentId = params.get(PARAM_COMMENT).toOptionalLong();
		requestId = params.get(PARAM_REQUEST).toOptionalLong();
	}
	
	private ObjectId getCommitId() {
		return getRepository().getObjectId(blobIdent.revision);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		newRevisionSelector(null);
		newCommentContext(null);
		newFileNavigator(null, null);
		newLastCommit(null);
		newFileViewer(null);
		
		add(new InstantSearchPanel("instantSearch", repoModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return blobIdent.revision;
			}
			
		}) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				BlobIdent selected = new BlobIdent(blobIdent.revision, hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits()); 
				RepoFilePage.this.onSelect(target, selected, hit.getTokenPos());
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
						return blobIdent.revision;
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
						return blobIdent;
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
				if (!indexManager.isIndexed(getRepository(), blobIdent.revision)) {
					final Long repoId = getRepository().getId();
					GitPlex.getInstance(UnitOfWork.class).asyncCall(new Runnable() {

						@Override
						public void run() {
							Repository repo = GitPlex.getInstance(Dao.class).load(Repository.class, repoId);
							GitPlex.getInstance(IndexManager.class).index(repo, blobIdent.revision);
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
			protected WebSocketTrait getTrait() {
				return trait;
			}

			@Override
			protected void onRender(WebSocketRequestHandler handler, WebSocketTrait trait) {
				handler.add(revisionIndexing);
				handler.appendJavaScript("$(window).resize();");
			}
			
		});
		if (getPullRequest() != null) {
			add(new PullRequestChangeRenderer() {

				@Override
				protected PullRequest getPullRequest() {
					return RepoFilePage.this.getPullRequest();
				}

			});
		}
	}
	
	@Override
	public Comment getComment() {
		return commentModel.getObject();
	}
	
	@Override
	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}
	
	private void newRevisionSelector(@Nullable AjaxRequestTarget target) {
		revisionSelector = new RevisionSelector(REVISION_SELECTOR_ID, repoModel, blobIdent.revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				BlobIdent selected = new BlobIdent();
				selected.revision = revision;
				selected.mode = FileMode.TREE.getBits();
				
				trait.revision = revision;
				
				if (selected.path != null) {
					try (	FileRepository jgitRepo = getRepository().openAsJGitRepo();
							RevWalk revWalk = new RevWalk(jgitRepo)) {
						RevTree revTree = revWalk.parseCommit(getCommitId()).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, selected.path, revTree);
						if (treeWalk != null) {
							selected.path = blobIdent.path;
							selected.mode = treeWalk.getRawMode(0);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				requestId = null;
				commentId = null;
				RepoFilePage.this.onSelect(target, selected, null);
				newRevisionSelector(target);
				newCommentContext(target);
				target.add(revisionIndexing);
			}

		};
		
		if (target != null) {
			replace(revisionSelector);
			target.add(revisionSelector);
		} else {
			add(revisionSelector);
		}
	}
	
	private void newCommentContext(@Nullable AjaxRequestTarget target) {
		Component commentContext = new WebMarkupContainer("commentContext") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest() != null);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new TooltipBehavior(new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						String tooltip = String.format("Inline comments added/displayed in "
								+ "this commit belong to pull request #%d (%s)", 
								request.getId(), request.getTitle());
						return tooltip;
					}
					
				}, new TooltipConfig().withPlacement(Placement.bottom)));
			}

		};
		commentContext.setOutputMarkupPlaceholderTag(true);
		if (target != null) {
			replace(commentContext);
			target.add(commentContext);
		} else {
			add(commentContext);
		}
	}
	
	private void newFileNavigator(@Nullable AjaxRequestTarget target, @Nullable BlobNameChangeCallback callback) {
		fileNavigator = new FileNavigator(FILE_NAVIGATOR_ID, repoModel, blobIdent, callback) {

			@Override
			protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
				RepoFilePage.this.onSelect(target, file, null);
			}

			@Override
			protected void onNewFile(AjaxRequestTarget target) {
				onAddOrEditFile(target);
			}
			
		};
		if (target != null) {
			replace(fileNavigator);
			target.add(fileNavigator);
		} else {
			add(fileNavigator);
		}
	}
	
	private void onAddOrEditFile(AjaxRequestTarget target) {
		ObjectId commitId = getRepository().getObjectId(blobIdent.revision);
		
		final String refName = Git.REFS_HEADS + blobIdent.revision;
		
		final AtomicReference<String> newPathRef = new AtomicReference<>(blobIdent.isTree()?null:blobIdent.path);
		
		fileViewer = new FileEditPanel(
				FILE_VIEWER_ID, repoModel, refName, 
				blobIdent.isTree()?null:blobIdent.path, 
						blobIdent.isTree()?"":getRepository().getBlob(blobIdent).getText().getContent(), 
				commitId) {

			@Override
			protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
				getRepository().cacheObjectId(blobIdent.revision, newCommitId);
				BlobIdent committed = new BlobIdent(
						blobIdent.revision, newPathRef.get(), FileMode.REGULAR_FILE.getBits());
				onSelect(target, committed, null);
				
	    		for (RepositoryListener listener: GitPlex.getExtensions(RepositoryListener.class))
	    			listener.onRefUpdate(getRepository(), refName, newCommitId.name());
			}

			@Override
			protected void onCancel(AjaxRequestTarget target) {
				lastCommit.setVisibilityAllowed(true);
				target.add(lastCommit);
				newFileViewer(target);
				newFileNavigator(target, null);
			}
			
		};
		final BlobNameChangeCallback callback = new BlobNameChangeCallback() {

			@Override
			public void onChange(AjaxRequestTarget target, String blobName) {
				String newPath;
				if (blobIdent.isTree()) {
					if (blobIdent.path != null)
						newPath = blobIdent.path + "/" + blobName;
					else
						newPath = blobName;
				} else {
					if (blobIdent.path.contains("/"))
						newPath = StringUtils.substringBeforeLast(blobIdent.path, "/") + "/" + blobName;
					else
						newPath = blobName;
				}
				newPathRef.set(GitUtils.normalizePath(newPath));
				((FileEditPanel)fileViewer).onNewPathChange(target, newPathRef.get());
			}
			
		};
		replace(fileViewer);
		target.add(fileViewer);
		lastCommit.setVisibilityAllowed(false);
		target.add(lastCommit);
		newFileNavigator(target, callback);
		target.appendJavaScript("$(window).resize();");
	}
	
	private void newLastCommit(@Nullable AjaxRequestTarget target) {
		lastCommit = new AjaxLazyLoadPanel(LAST_COMMIT_ID) {
			
			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
				String html = "<img src='" + RequestCycle.get().urlFor(handler) + "' class='loading'/> Loading latest commit...";
				return new Label(markupId, html).setEscapeModelStrings(false);
			}

			@Override
			protected void onComponentLoaded(Component component, AjaxRequestTarget target) {
				super.onComponentLoaded(component, target);
				target.appendJavaScript("$(window).resize();");
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				return new LastCommitPanel(markupId, repoModel, blobIdent);
			}
		};
		lastCommit.setOutputMarkupPlaceholderTag(true);
		if (target != null) {
			replace(lastCommit);
			target.add(lastCommit);
		} else {
			add(lastCommit);
		}
	}
	
	private BlobViewPanel renderBlobViewer(String panelId) {
		for (BlobRenderer renderer: GitPlex.getExtensions(BlobRenderer.class)) {
			BlobViewPanel panel = renderer.render(panelId, this);
			if (panel != null)
				return panel;
		}
				
		throw new IllegalStateException("No applicable blob renderer found.");
	}
	
	private void newFileViewer(@Nullable AjaxRequestTarget target) {
		if (blobIdent.path == null || blobIdent.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, repoModel, blobIdent) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
					RepoFilePage.this.onSelect(target, file, null);
				}
				
			};
		} else {
			fileViewer = renderBlobViewer(FILE_VIEWER_ID);
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
		PageParameters params = paramsOf(getRepository(), blobIdent.revision, blobIdent.path, 
				blame, commentId, requestId);
		CharSequence url = RequestCycle.get().urlFor(RepoFilePage.class, params);
		HistoryState state = new HistoryState();
		state.blame = blame;
		state.commentId = commentId;
		state.requestId = requestId;
		state.file = blobIdent;
		state.tokenPos = tokenPos;
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

	public static PageParameters paramsOf(Repository repository, BlobIdent blobIdent) {
		return paramsOf(repository, blobIdent.revision, blobIdent.path);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		return paramsOf(repository, revision, path, false, null, null);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision, 
			@Nullable String path, boolean blame, @Nullable Long commentId, @Nullable Long requestId) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		if (blame)
			params.set(PARAM_BLAME, blame);
		if (commentId != null)
			params.set(PARAM_COMMENT, commentId);
		if (requestId != null)
			params.set(PARAM_REQUEST, requestId);
		return params;
	}
	
	public static PageParameters paramsOf(PullRequest request, String revision, @Nullable String path) {
		return paramsOf(request.getTargetRepo(), revision, path, false, null, request.getId());
	}
	
	public static PageParameters paramsOf(Comment comment) {
		return paramsOf(comment.getRepository(), comment.getBlobIdent().revision, 
				comment.getBlobIdent().path, false, comment.getId(), null);
	}
	
	private void renderSearchResult(AjaxRequestTarget target, List<QueryHit> hits) {
		Component searchResult = newSearchResult(hits);
		
		replace(searchResult);
		target.add(searchResult);
		target.appendJavaScript(""
				+ "$('#repo-file>.search-result').show(); "
				+ "$('#repo-file .search-result>.body').focus(); "
				+ "$(window).resize();");
	}
	
	private Component newSearchResult(List<QueryHit> hits) {
		return new SearchResultPanel(SEARCH_RESULD_ID, hits) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				if (hit.getBlobPath().equals(blobIdent.path) && fileViewer instanceof SourceViewPanel) {
					tokenPos = hit.getTokenPos();
					SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
					if (tokenPos != null || blame) {
						sourceViewer.highlightToken(target, tokenPos);
					} else {
						BlobViewPanel blobViewer = renderBlobViewer(FILE_VIEWER_ID);
						if (blobViewer instanceof SourceViewPanel) {
							sourceViewer.highlightToken(target, tokenPos);
						} else {
							fileViewer.replaceWith(blobViewer);
							fileViewer = blobViewer;
							target.add(fileViewer);
							target.appendJavaScript("$(window).resize();");
						}
					}
					pushState(target);
				} else {
					BlobIdent selected = new BlobIdent(blobIdent.revision, hit.getBlobPath(), 
							FileMode.REGULAR_FILE.getBits());
					RepoFilePage.this.onSelect(target, selected, hit.getTokenPos());
				}
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
		super.onPopState(target, data);
		
		HistoryState state = (HistoryState) data;
		blobIdent = state.file;
		blame = state.blame;
		commentId = state.commentId;
		requestId = state.requestId;
		tokenPos = state.tokenPos;
		
		trait.revision = blobIdent.revision;

		target.add(revisionIndexing);
		newRevisionSelector(target);
		newCommentContext(target);
		newFileNavigator(target, null);
		newLastCommit(target);
		newFileViewer(target);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoFilePage.class, paramsOf(repository));
	}
	
	private static class RevisionIndexed implements WebSocketTrait {

		Long repoId;
		
		volatile String revision;
		
		@Override
		public boolean is(WebSocketTrait trait) {
			if (trait == null || !(trait instanceof RevisionIndexed))  
				return false;  
			RevisionIndexed other = (RevisionIndexed) trait;  
		    return Objects.equal(repoId, other.repoId) && Objects.equal(revision, other.revision);
		}
		
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		commentModel.detach();
		
		super.onDetach();
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

	@Override
	public BlobIdent getBlobIdent() {
		return blobIdent;
	}

	@Override
	public TokenPosition getTokenPos() {
		return tokenPos;
	}

	@Override
	public boolean isBlame() {
		return blame;
	}

	@Override
	public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, TokenPosition tokenPos) {
		this.blobIdent = blobIdent; 
		this.tokenPos = tokenPos;
		blame = false;
		
		newFileNavigator(target, null);
		newLastCommit(target);
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
		
		if (fileViewer instanceof SourceViewPanel) {
			SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
			if (blame || tokenPos != null) {
				sourceViewer.onBlameChange(target);
			} else {
				BlobViewPanel blobViewer = renderBlobViewer(FILE_VIEWER_ID);
				if (blobViewer instanceof SourceViewPanel) {
					sourceViewer.onBlameChange(target);
				} else {
					fileViewer.replaceWith(blobViewer);
					fileViewer = blobViewer;
					target.add(fileViewer);
					target.appendJavaScript("$(window).resize();");
				}
			}
		} else {
			newFileViewer(target);
		}

		pushState(target);
	}

	@Override
	public void onDelete(AjaxRequestTarget target) {
		ObjectId commitId = getRepository().getObjectId(blobIdent.revision);
		
		final String refName = Git.REFS_HEADS + blobIdent.revision;

		CancelListener cancelListener = new CancelListener() {

			@Override
			public void onCancel(AjaxRequestTarget target) {
				lastCommit.setVisibilityAllowed(true);
				target.add(lastCommit);
				newFileViewer(target);
			}
			
		};
		fileViewer = new EditSavePanel(FILE_VIEWER_ID, repoModel, refName, blobIdent.path, 
				null, commitId, cancelListener) {

			@Override
			protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
				getRepository().cacheObjectId(blobIdent.revision, newCommitId);
				try (	FileRepository jgitRepo = getRepository().openAsJGitRepo();
						RevWalk revWalk = new RevWalk(jgitRepo)) {
					RevTree revTree = revWalk.parseCommit(newCommitId).getTree();
					String parentPath = StringUtils.substringBeforeLast(blobIdent.path, "/");
					while (TreeWalk.forPath(jgitRepo, parentPath, revTree) == null) {
						if (parentPath.contains("/")) {
							parentPath = StringUtils.substringBeforeLast(parentPath, "/");
						} else {
							parentPath = null;
							break;
						}
					}
					BlobIdent parentBlobIdent = new BlobIdent(blobIdent.revision, parentPath, 
							FileMode.TREE.getBits());
					onSelect(target, parentBlobIdent, null);

					for (RepositoryListener listener: GitPlex.getExtensions(RepositoryListener.class))
		    			listener.onRefUpdate(getRepository(), refName, newCommitId.name());
					
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		};
		lastCommit.setVisibilityAllowed(false);
		target.add(lastCommit);
		replace(fileViewer);
		target.add(fileViewer);
		target.appendJavaScript("$(window).resize();");
	}

	@Override
	public void onEdit(AjaxRequestTarget target) {
		onAddOrEditFile(target);
	}
	
}

package com.pmease.gitplex.web.page.repository.file;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
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
import com.pmease.gitplex.web.WebSession;
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

	private static class SearchResultKey extends MetaDataKey<ArrayList<QueryHit>> {
	};
	
	public static final SearchResultKey SEARCH_RESULT_KEY = new SearchResultKey();		
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MODE = "mode";
	
	private static final String PARAM_HIGHLIGHT = "highlight";
	
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
	
	private Highlight highlight;
	
	private Mode mode;
	
	private Component commentContext;
	
	private Component revisionIndexing;
	
	private WebMarkupContainer searchResultContainer;
	
	private AtomicReference<String> newPathRef;	
	
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
		
		String modeStr = params.get(PARAM_MODE).toString();
		if (modeStr != null)
			mode = Mode.valueOf(modeStr.toUpperCase());
		String highlightStr = params.get(PARAM_HIGHLIGHT).toString();
		if (highlightStr != null)
			highlight = new Highlight(highlightStr);
		
		commentId = params.get(PARAM_COMMENT).toOptionalLong();
		requestId = params.get(PARAM_REQUEST).toOptionalLong();
		
		if ((requestId != null || commentId != null) && !GitUtils.isHash(blobIdent.revision))
			throw new IllegalArgumentException("Pull request can only be associated with a hash revision");
	}
	
	private ObjectId getCommitId() {
		return getRepository().getObjectId(blobIdent.revision);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new InstantSearchPanel("instantSearch", repoModel, requestModel, new AbstractReadOnlyModel<String>() {

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
			protected void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits) {
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
		
		add(newRevisionSelector());
		
		add(commentContext = new WebMarkupContainer("commentContext") {

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
				
				setOutputMarkupPlaceholderTag(true);
			}

		});
		
		add(newFileNavigator());
		
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

		add(newLastCommit());
		add(newFileViewer());

		add(searchResultContainer = new WebMarkupContainer("searchResultContainer"));
		
		List<QueryHit> hits = WebSession.get().getMetaData(SEARCH_RESULT_KEY);
		if (hits != null) { 
			WebSession.get().setMetaData(SEARCH_RESULT_KEY, null);
			searchResultContainer.add(newSearchResult(hits));
		} else {
			searchResultContainer.add(new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true));
			searchResultContainer.add(AttributeAppender.replace("style", "display: none;"));
		}
		
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
	
	private void updateFileNavigator(AjaxRequestTarget target) {
		Component fileNavigator = newFileNavigator();
		replace(fileNavigator);
		target.add(fileNavigator);
	}
	
	private Component newFileNavigator() {
		final BlobNameChangeCallback callback;
		
		if (mode == Mode.EDIT) {
			callback = new BlobNameChangeCallback() {

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
					((FileEditPanel) get(FILE_VIEWER_ID)).onNewPathChange(target, newPathRef.get());
				}
				
			};
		} else {
			callback = null;
		}
		
		return new FileNavigator(FILE_NAVIGATOR_ID, repoModel, blobIdent, callback) {

			@Override
			protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
				RepoFilePage.this.onSelect(target, file, null);
			}

			@Override
			protected void onNewFile(AjaxRequestTarget target) {
				mode = Mode.EDIT;
				
				updateFileNavigator(target);
				updateFileViewer(target);
				
				pushState(target);
				resizeWindow(target);
			}
			
		};
	}
	
	private void updateLastCommit(AjaxRequestTarget target) {
		Component lastCommit = newLastCommit();
		replace(lastCommit);
		target.add(lastCommit);
	}
	
	private Component newLastCommit() {
		return new AjaxLazyLoadPanel(LAST_COMMIT_ID) {
			
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

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(mode != Mode.EDIT);
			}

		};
	}
	
	private BlobViewPanel renderBlobViewer(String panelId) {
		for (BlobRenderer renderer: GitPlex.getExtensions(BlobRenderer.class)) {
			BlobViewPanel panel = renderer.render(panelId, this);
			if (panel != null)
				return panel;
		}
				
		throw new IllegalStateException("No applicable blob renderer found.");
	}
	
	private void updateFileViewer(AjaxRequestTarget target) {
		Component fileViewer = newFileViewer();
		replace(fileViewer);
		target.add(fileViewer);
	}
	
	private Component newFileViewer() {
		if (mode == Mode.EDIT) {
			final String refName = GitUtils.branch2ref(blobIdent.revision);
			newPathRef = new AtomicReference<>(blobIdent.isTree()?null:blobIdent.path);			
			return new FileEditPanel(
					FILE_VIEWER_ID, repoModel, refName, 
					blobIdent.isTree()?null:blobIdent.path, 
					blobIdent.isTree()?"":getRepository().getBlob(blobIdent).getText().getContent(), 
							getRepository().getObjectId(blobIdent.revision)) {
 
				@Override
				protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
					Repository repository = getRepository();
					String branch = blobIdent.revision;
					repository.cacheObjectId(branch, newCommitId);
					BlobIdent committed = new BlobIdent(
							branch, newPathRef.get(), FileMode.REGULAR_FILE.getBits());
		    		for (RepositoryListener listener: GitPlex.getExtensions(RepositoryListener.class))
		    			listener.onRefUpdate(repository, refName, newCommitId.name());

		    		if (repository.equals(getRepository())) {
		    			HistoryState state = getState();
		    			state.blobIdent = committed;
		    			state.mode = null;
		    			applyState(target, state);
		    			pushState(target);
		    			resizeWindow(target);
		    		} else {
		    			setResponsePage(RepoFilePage.class, paramsOf(repository, committed));
		    		}
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					mode = null;
					pushState(target);
					resizeWindow(target);
				}
				
			};
		} else if (mode == Mode.DELETE) {
			final String refName = GitUtils.branch2ref(blobIdent.revision);

			CancelListener cancelListener = new CancelListener() {

				@Override
				public void onCancel(AjaxRequestTarget target) {
					mode = null;

					updateFileViewer(target);
					
					pushState(target);
					resizeWindow(target);
				}
				
			};
			
			return new EditSavePanel(FILE_VIEWER_ID, repoModel, refName, blobIdent.path, 
					null, getRepository().getObjectId(blobIdent.revision), cancelListener) {

				@Override
				protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
					Repository repository = getRepository();
					String branch = blobIdent.revision;
					repository.cacheObjectId(branch, newCommitId);
					try (	FileRepository jgitRepo = repository.openAsJGitRepo();
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
						for (RepositoryListener listener: GitPlex.getExtensions(RepositoryListener.class))
			    			listener.onRefUpdate(repository, refName, newCommitId.name());
						BlobIdent parentBlobIdent = new BlobIdent(branch, parentPath, FileMode.TREE.getBits());
						if (repository.equals(getRepository())) {
							HistoryState state = getState();
							state.blobIdent = parentBlobIdent;
							state.mode = null;
							applyState(target, state);
							pushState(target);
							resizeWindow(target);
						} else {
							setResponsePage(RepoFilePage.class, paramsOf(repository, parentBlobIdent));
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
			};
		} else if (blobIdent.path == null || blobIdent.isTree()) {
			return new FileListPanel(FILE_VIEWER_ID, repoModel, blobIdent) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
					RepoFilePage.this.onSelect(target, file, null);
				}
				
			};
		} else {
			return renderBlobViewer(FILE_VIEWER_ID);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getRepository(), blobIdent.revision, blobIdent.path, 
				highlight, commentId, requestId, mode);
		CharSequence url = RequestCycle.get().urlFor(RepoFilePage.class, params);
		pushState(target, url.toString(), getState());
	}
	
	private HistoryState getState() {
		HistoryState state = new HistoryState();
		state.blobIdent = new BlobIdent(blobIdent);
		state.commentId = commentId;
		state.highlight = new Highlight(highlight);
		state.mode = mode;
		state.requestId = requestId;
		return state;
	}
	
	private void setState(HistoryState state) {
		blobIdent = new BlobIdent(state.blobIdent);
		commentId = state.commentId;
		highlight = state.highlight;
		mode = state.mode;
		requestId = state.requestId;
	}
	
	private void updateRevisionSelector(AjaxRequestTarget target) {
		Component revisionSelector = newRevisionSelector();
		replace(revisionSelector);
		target.add(revisionSelector);
	}
	
	private Component newRevisionSelector() {
		return new RevisionSelector(REVISION_SELECTOR_ID, repoModel, blobIdent.revision) {

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				HistoryState state = getState();
				state.blobIdent.revision = revision;
				state.requestId = null;
				state.commentId = null;
				state.mode = null;
				state.highlight = null;
				
				if (state.blobIdent.path != null) {
					try (	FileRepository jgitRepo = getRepository().openAsJGitRepo();
							RevWalk revWalk = new RevWalk(jgitRepo)) {
						RevTree revTree = revWalk.parseCommit(getCommitId()).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(jgitRepo, blobIdent.path, revTree);
						if (treeWalk != null) {
							state.blobIdent.mode = treeWalk.getRawMode(0);
						} else {
							state.blobIdent.path = null;
							state.blobIdent.mode = FileMode.TREE.getBits();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

				applyState(target, state);
				pushState(target);
				resizeWindow(target);
			}

		};
	}

	private void applyState(AjaxRequestTarget target, HistoryState state) {
		setState(state);
		trait.revision = blobIdent.revision;

		updateRevisionSelector(target);
		
		target.add(commentContext);
		
		updateFileNavigator(target);
		
		target.add(revisionIndexing);
		
		updateLastCommit(target);
		updateFileViewer(target);
		
		resizeWindow(target);
		
		/*
		if (!Objects.equal(state.blobIdent.revision, blobIdent.revision) 
				|| !Objects.equal(state.requestId, requestId)
				|| !Objects.equal(state.commentId, commentId)) {
			setState(state);
			trait.revision = blobIdent.revision;

			Component revisionSelector = newRevisionSelector(blobIdent.revision);
			replace(revisionSelector);
			target.add(revisionSelector);
			target.add(commentContext);
			
			Component fileNavigator = newFileNavigator();
			replace(fileNavigator);
			target.add(fileNavigator);
			
			target.add(revisionIndexing);
			
			Component lastCommit = newLastCommit();
			replace(lastCommit);
			target.add(lastCommit);
			
			Component fileViewer = newFileViewer();
			replace(fileViewer);
			target.add(fileViewer);
			
			if (querySymbol != null)
				updateSearchResult(target, queryOccurrences(querySymbol));
			else 
				updateSearchResult(target, null);

			resize = true;
		} else {
			if (!Objects.equal(state.blobIdent.path, blobIdent.path)) {
				resize = true;
				blobIdent = new BlobIdent(state.blobIdent);
				highlight = state.highlight;
				mode = state.mode;
				
				Component fileNavigator = newFileNavigator();
				replace(fileNavigator);
				target.add(fileNavigator);
				
				Component lastCommit = newLastCommit();
				replace(lastCommit);
				target.add(lastCommit);
				
				Component fileViewer = newFileViewer();
				replace(fileViewer);
				target.add(fileViewer);
			} else if (mode == null || mode == Mode.BLAME) {
				if (state.mode == Mode.EDIT || state.mode == Mode.DELETE) {
					resize = true;
					highlight = state.highlight;
					mode = state.mode;
					
					if (state.mode == Mode.EDIT) {
						Component fileNavigator = newFileNavigator();
						replace(fileNavigator);
						target.add(fileNavigator);
					}
					
					Component fileViewer = newFileViewer();
					replace(fileViewer);
					target.add(fileViewer);
				} else if (mode != state.mode) {
					resize = true;
					highlight = state.highlight;
					mode = state.mode;
					Component fileViewer = newFileViewer();
					replace(fileViewer);
					target.add(fileViewer);
				}
			} else if ((mode == null || mode == Mode.BLAME) && (state.mode == Mode.EDIT || state.mode == Mode.DELETE) 
					|| ((mode == Mode.EDIT || mode == Mode.DELETE) && (state.mode == null || state.mode == Mode.BLAME))) {
				resize = true;
				highlight = state.highlight;
				mode = state.mode;

				Component fileNavigator = newFileNavigator();
				replace(fileNavigator);
				target.add(fileNavigator);
				
				Component fileViewer = newFileViewer();
				replace(fileViewer);
				target.add(fileViewer);
			} else {
				if (!Objects.equal(state.mode, mode)) {
					resize = true;
					mode = state.mode;
					Component fileViewer = newFileViewer();
					replace(fileViewer);
					target.add(fileViewer);
				} 
				
				if (!Objects.equal(state.highlight, highlight)) {
					highlight = state.highlight;
					Component fileViewer = get(FILE_VIEWER_ID);
					if (fileViewer instanceof SourceViewPanel) {
						SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
						if (highlight == null && mode != Mode.BLAME) {
							BlobViewPanel blobViewer = renderBlobViewer(FILE_VIEWER_ID);
							if (blobViewer instanceof SourceViewPanel) {
								sourceViewer.highlight(target, highlight);
							} else {
								fileViewer.replaceWith(blobViewer);
								fileViewer = blobViewer;
								target.add(fileViewer);
								target.appendJavaScript("$(window).resize();");
							}
						} else {
							sourceViewer.highlight(target, highlight);
						}
					} else {
						
					}
				}
			}
			
			if (!Objects.equal(state.querySymbol, querySymbol)) {
				resize = true;
				querySymbol = state.querySymbol;
				if (querySymbol != null)
					updateSearchResult(target, queryOccurrences(querySymbol));
				else 
					updateSearchResult(target, null);
			}
		}
		
		if (resize)
			target.appendJavaScript("$(window).resize();");
		if (pushState)
			pushState(target, state);
		*/
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

	public static PageParameters paramsOf(PullRequest request, BlobIdent blobIdent) {
		return paramsOf(request, blobIdent.revision, blobIdent.path);
	}
	
	public static PageParameters paramsOf(PullRequest request, String commitHash, @Nullable String path) {
		return paramsOf(request.getTargetRepo(), commitHash, path, null, null, request.getId(), null);
	}
	
	public static PageParameters paramsOf(Comment comment) {
		return paramsOf(comment.getRepository(), comment.getBlobIdent().revision, 
				comment.getBlobIdent().path, null, comment.getId(), null, null);
	}
	
	public static PageParameters paramsOf(Repository repository, BlobIdent blobIdent) {
		return paramsOf(repository, blobIdent.revision, blobIdent.path);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision, @Nullable String path) {
		return paramsOf(repository, revision, path, null, null, null, null);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision, 
			@Nullable String path, @Nullable Mode mode) {
		return paramsOf(repository, revision, path, null, null, null, mode);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision,
			@Nullable String path, @Nullable Highlight highlight, @Nullable Long requestId) {
		return paramsOf(repository, revision, path, highlight, null, requestId, null);
	}
	
	public static PageParameters paramsOf(Repository repository, @Nullable String revision,
			@Nullable String path, @Nullable Highlight highlight, @Nullable Long commentId, 
			@Nullable Long requestId, @Nullable Mode mode) {
		PageParameters params = paramsOf(repository);
		if (revision != null)
			params.set(PARAM_REVISION, revision);
		if (path != null)
			params.set(PARAM_PATH, path);
		if (highlight != null)
			params.set(PARAM_HIGHLIGHT, highlight.toString());
		if (commentId != null)
			params.set(PARAM_COMMENT, commentId);
		if (requestId != null)
			params.set(PARAM_REQUEST, requestId);
		if (mode != null)
			params.set(PARAM_MODE, mode.name().toLowerCase());
		return params;
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
		return new SearchResultPanel(SEARCH_RESULD_ID, this, hits) {
			
			@Override
			protected void onClose(AjaxRequestTarget target) {
				Component searchResult = new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true);
				target.appendJavaScript("$('#repo-file>.search-result').hide();");
				searchResultContainer.replace(searchResult);
				target.add(searchResult);
				resizeWindow(target);
			}
			
		};
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		applyState(target, (HistoryState) data);
		resizeWindow(target);
	}
	
	private void resizeWindow(AjaxRequestTarget target) {
		target.appendJavaScript("$(window).resize();");
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
	public Highlight getHighlight() {
		return highlight;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable TokenPosition tokenPos) {
		highlight = Highlight.of(tokenPos);
		Component fileViewer = get(FILE_VIEWER_ID);
		if (fileViewer instanceof SourceViewPanel && this.blobIdent.path.equals(blobIdent.path)) {
			SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
			if (highlight == null && mode != Mode.BLAME) {
				BlobViewPanel blobViewer = renderBlobViewer(FILE_VIEWER_ID);
				if (blobViewer instanceof SourceViewPanel) {
					sourceViewer.highlight(target, highlight);
				} else {
					fileViewer.replaceWith(blobViewer);
					fileViewer = blobViewer;
					target.add(fileViewer);
					resizeWindow(target);
				}
			} else {
				sourceViewer.highlight(target, highlight);
			}
			pushState(target);
		} else {
			this.blobIdent = blobIdent; 
			mode = null;
			
			updateFileNavigator(target);
			updateLastCommit(target);
			
			updateFileViewer(target);
			
			pushState(target);
			resizeWindow(target);
		}
	}

	@Override
	public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
		Component searchResult = newSearchResult(hits);
		searchResultContainer.replace(searchResult);
		target.add(searchResult);
		target.appendJavaScript(""
				+ "$('#repo-file>.search-result').show();"
				+ "$('#repo-file .search-result>.body').focus();"
				+ "$(window).resize();");
	}

	@Override
	public void onBlameChange(AjaxRequestTarget target) {
		HistoryState state = getState();
		if (mode == null)
			state.mode = Mode.BLAME;
		else
			state.mode = null;
		applyState(target, state);
	}

	@Override
	public void onDelete(AjaxRequestTarget target) {
		mode = Mode.EDIT;
		
		Component fileViewer = newFileViewer();
		replace(fileViewer);
		target.add(fileViewer);
		
		pushState(target);
		resizeWindow(target);
	}

	@Override
	public void onEdit(AjaxRequestTarget target) {
		mode = Mode.EDIT;
		
		updateFileNavigator(target);
		
		Component fileViewer = newFileViewer();
		replace(fileViewer);
		target.add(fileViewer);
		
		pushState(target);
		resizeWindow(target);
	}

	@Override
	public boolean isOnBranch() {
		return getRepository().getRefs(Git.REFS_HEADS).containsKey(blobIdent.revision);
	}

	@Override
	public boolean isAtSourceBranchHead() {
		PullRequest request = getPullRequest();
		return request != null && request.getSourceRepo() != null 
				&& blobIdent.revision.equals(request.getSource().getHead(false)); 
	}
	
}

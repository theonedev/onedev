package com.pmease.gitplex.web.page.depot.file;

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
import org.apache.wicket.markup.html.link.ResourceLink;
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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.BlobIdent;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.exception.ObjectNotExistException;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.lang.extractors.TokenPosition;
import com.pmease.commons.wicket.assets.closestdescendant.ClosestDescendantResourceReference;
import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.modal.ModalLink;
import com.pmease.commons.wicket.websocket.WebSocketRenderBehavior;
import com.pmease.commons.wicket.websocket.WebSocketTrait;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Comment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.extensionpoint.RefListener;
import com.pmease.gitplex.search.IndexListener;
import com.pmease.gitplex.search.IndexManager;
import com.pmease.gitplex.search.SearchManager;
import com.pmease.gitplex.search.hit.QueryHit;
import com.pmease.gitplex.search.query.BlobQuery;
import com.pmease.gitplex.search.query.TextQuery;
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
import com.pmease.gitplex.web.component.revisionpicker.RevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.resource.ArchiveResource;
import com.pmease.gitplex.web.resource.ArchiveResourceReference;
import com.pmease.gitplex.web.websocket.PullRequestChangeRenderer;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.jqueryui.JQueryUIResizableJavaScriptReference;

@SuppressWarnings("serial")
public class DepotFilePage extends DepotPage implements BlobViewContext {

	private static class SearchResultKey extends MetaDataKey<ArrayList<QueryHit>> {
	};
	
	public static final SearchResultKey SEARCH_RESULT_KEY = new SearchResultKey();		
	
	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_REQUEST = "request";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MODE = "mode";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_CLIENT_STATE = "client_state";
	
	private static final String PARAM_MARK = "mark";
	
	private static final String REVISION_PICKER_ID = "revisionPicker";
	
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
	
	private Mark mark;
	
	private Mode mode;
	
	private Component commentContext;
	
	private Component revisionIndexing;
	
	private WebMarkupContainer searchResultContainer;
	
	private AtomicReference<String> newPathRef;	
	
	private final RevisionIndexed trait = new RevisionIndexed();
	
	private transient List<QueryHit> queryHits;
	
	// client state holding CodeMirror cursor, scroll, marks, etc.
	private transient String clientState;

	public DepotFilePage(final PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));
		
		trait.depotId = getDepot().getId();
		
		commentId = params.get(PARAM_COMMENT).toOptionalLong();
		blobIdent.revision = params.get(PARAM_REVISION).toString();
		blobIdent.path = GitUtils.normalizePath(params.get(PARAM_PATH).toString());
		
		Comment comment = commentModel.getObject();
		if (comment != null) {
			if (blobIdent.revision != null || blobIdent.path != null)
				throw new IllegalArgumentException("Revision or path should not be specified if comment is specified");
			blobIdent = comment.getBlobIdent();
		} else {
			requestId = params.get(PARAM_REQUEST).toOptionalLong();
			
			blobIdent.revision = GitUtils.normalizePath(params.get(PARAM_REVISION).toString());
			if (blobIdent.revision == null)
				blobIdent.revision = getDepot().getDefaultBranch();

			if (requestId != null && !GitUtils.isHash(blobIdent.revision))
				throw new IllegalArgumentException("Pull request can only be associated with a hash revision");
		}
		
		trait.revision = blobIdent.revision;
		
		if (blobIdent.path != null) {
			try (	Repository repository = getDepot().openRepository();
					RevWalk revWalk = new RevWalk(repository)) {
				RevTree revTree = getDepot().getRevCommit(getCommitId(), true).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, blobIdent.path, revTree);
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
		String markStr = params.get(PARAM_MARK).toString();
		if (markStr != null)
			mark = new Mark(markStr);
		
		queryHits = WebSession.get().getMetaData(SEARCH_RESULT_KEY);
		if (queryHits != null) { 
			WebSession.get().setMetaData(SEARCH_RESULT_KEY, null);
		} else {
			String symbol = params.get(PARAM_QUERY).toString();
			if (symbol != null) {
				BlobQuery query = new TextQuery(symbol, false, true, true, 
						null, null, SearchResultPanel.MAX_QUERY_ENTRIES);
				try {
					SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
					queryHits = searchManager.search(depotModel.getObject(), blobIdent.revision, query);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}								
			}
		}
		
		clientState = params.get(PARAM_CLIENT_STATE).toString();
	}
	
	private ObjectId getCommitId() {
		return getDepot().getObjectId(blobIdent.revision);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new InstantSearchPanel("instantSearch", depotModel, requestModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return blobIdent.revision;
			}
			
		}) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				BlobIdent selected = new BlobIdent(blobIdent.revision, hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits()); 
				DepotFilePage.this.onSelect(target, selected, hit.getTokenPos());
			}
			
			@Override
			protected void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				newSearchResult(target, hits);
				resizeWindow(target);
			}
			
		});
		
		add(new ModalLink("advancedSearch") {

			@Override
			protected Component newContent(String id) {
				return new AdvancedSearchPanel(id, depotModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return blobIdent.revision;
					}
					
				}) {

					@Override
					protected void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
						newSearchResult(target, hits);
						resizeWindow(target);
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
			
		});
		
		newDownloadLink(null);
		
		newRevisionPicker(null);
		
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
		
		newFileNavigator(null);
		
		add(revisionIndexing = new WebMarkupContainer("revisionIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Image("icon", new PackageResourceReference(DepotFilePage.class, "indexing.gif")));
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				if (!indexManager.isIndexed(getDepot(), blobIdent.revision)) {
					GitPlex.getInstance(IndexManager.class).index(getDepot(), blobIdent.revision);
					setVisible(true);
				} else {
					setVisible(false);
				}
			}
			
		});

		newLastCommit(null);
		newFileViewer(null, clientState);

		add(searchResultContainer = new WebMarkupContainer("searchResultContainer"));
		
		newSearchResult(null, queryHits);
		
		add(new WebSocketRenderBehavior() {
			
			@Override
			protected WebSocketTrait getTrait() {
				return trait;
			}

			@Override
			protected void onRender(WebSocketRequestHandler handler, WebSocketTrait trait) {
				handler.add(revisionIndexing);
				resizeWindow(handler);
			}
			
		});
		if (getPullRequest() != null) {
			add(new PullRequestChangeRenderer() {

				@Override
				protected PullRequest getPullRequest() {
					return DepotFilePage.this.getPullRequest();
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
	
	private void newFileNavigator(@Nullable AjaxRequestTarget target) {
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
		
		Component fileNavigator = new FileNavigator(FILE_NAVIGATOR_ID, depotModel, requestModel, blobIdent, callback) {

			@Override
			protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
				DepotFilePage.this.onSelect(target, file, null);
			}

			@Override
			protected void onNewFile(AjaxRequestTarget target) {
				mode = Mode.EDIT;
				
				newFileNavigator(target);
				newFileViewer(target, null);
				
				pushState(target);
				resizeWindow(target);
			}
			
		};
		if (target != null) {
			replace(fileNavigator);
			target.add(fileNavigator);
		} else {
			add(fileNavigator);
		}
	}
	
	private void newLastCommit(@Nullable AjaxRequestTarget target) {
		Component lastCommit = new AjaxLazyLoadPanel(LAST_COMMIT_ID) {
			
			@Override
			public Component getLoadingComponent(String markupId) {
				IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
				String html = "<img src='" + RequestCycle.get().urlFor(handler) + "' class='loading'/> Loading latest commit...";
				return new Label(markupId, html).setEscapeModelStrings(false);
			}

			@Override
			protected void onComponentLoaded(Component component, AjaxRequestTarget target) {
				super.onComponentLoaded(component, target);
				resizeWindow(target);
			}

			@Override
			public Component getLazyLoadComponent(String markupId) {
				return new LastCommitPanel(markupId, depotModel, blobIdent);
			}

		};
		if (target != null) {
			replace(lastCommit);
			target.add(lastCommit);
		} else {
			add(lastCommit);
		}
	}
	
	private BlobViewPanel renderBlobViewer(String panelId, @Nullable String clientState) {
		for (BlobRenderer renderer: GitPlex.getExtensions(BlobRenderer.class)) {
			BlobViewPanel panel = renderer.render(panelId, this, clientState);
			if (panel != null)
				return panel;
		}
				
		throw new IllegalStateException("No applicable blob renderer found.");
	}
	
	private void newFileViewer(@Nullable AjaxRequestTarget target, @Nullable String clientState) {
		Component fileViewer;
		if (mode == Mode.EDIT) {
			final String refName = GitUtils.branch2ref(blobIdent.revision);
			newPathRef = new AtomicReference<>(blobIdent.isTree()?null:blobIdent.path);			
			fileViewer = new FileEditPanel(
					FILE_VIEWER_ID, depotModel, refName, 
					blobIdent.isTree()?null:blobIdent.path, 
					blobIdent.isTree()?"":getDepot().getBlob(blobIdent).getText().getContent(), 
							getDepot().getObjectId(blobIdent.revision), mark, clientState) {
 
				@Override
				protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
					Depot depot = getDepot();
					String branch = blobIdent.revision;
					depot.cacheObjectId(branch, newCommitId);
					BlobIdent committed = new BlobIdent(
							branch, newPathRef.get(), FileMode.REGULAR_FILE.getBits());
		    		for (RefListener listener: GitPlex.getExtensions(RefListener.class))
		    			listener.onRefUpdate(depot, refName, newCommitId.name());

		    		HistoryState state = getState();
	    			state.blobIdent = committed;
	    			state.mode = null;
	    			applyState(target, state);
	    			pushState(target);
	    			resizeWindow(target);
				}

				@Override
				protected void onCancel(AjaxRequestTarget target) {
					mode = null;
					newFileNavigator(target);
					newFileViewer(target, null);
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

					newFileViewer(target, null);
					
					pushState(target);
					resizeWindow(target);
				}
				
			};
			
			fileViewer = new EditSavePanel(FILE_VIEWER_ID, depotModel, refName, blobIdent.path, 
					null, getDepot().getObjectId(blobIdent.revision), cancelListener) {

				@Override
				protected void onCommitted(AjaxRequestTarget target, ObjectId newCommitId) {
					Depot depot = getDepot();
					String branch = blobIdent.revision;
					depot.cacheObjectId(branch, newCommitId);
					try (	Repository repository = depot.openRepository();
							RevWalk revWalk = new RevWalk(repository)) {
						RevTree revTree = getDepot().getRevCommit(newCommitId, true).getTree();
						String parentPath = StringUtils.substringBeforeLast(blobIdent.path, "/");
						while (TreeWalk.forPath(repository, parentPath, revTree) == null) {
							if (parentPath.contains("/")) {
								parentPath = StringUtils.substringBeforeLast(parentPath, "/");
							} else {
								parentPath = null;
								break;
							}
						}
						for (RefListener listener: GitPlex.getExtensions(RefListener.class))
			    			listener.onRefUpdate(depot, refName, newCommitId.name());
						BlobIdent parentBlobIdent = new BlobIdent(branch, parentPath, FileMode.TREE.getBits());
						HistoryState state = getState();
						state.blobIdent = parentBlobIdent;
						state.mode = null;
						applyState(target, state);
						pushState(target);
						resizeWindow(target);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
			};
		} else if (blobIdent.path == null || blobIdent.isTree()) {
			fileViewer = new FileListPanel(FILE_VIEWER_ID, depotModel, requestModel, blobIdent) {

				@Override
				protected void onSelect(AjaxRequestTarget target, BlobIdent file) {
					DepotFilePage.this.onSelect(target, file, null);
				}
				
			};
		} else {
			fileViewer = renderBlobViewer(FILE_VIEWER_ID, clientState);
		}
		if (target != null) {
			replace(fileViewer);
			target.add(fileViewer);
		} else {
			add(fileViewer);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), getState());
		CharSequence url = RequestCycle.get().urlFor(DepotFilePage.class, params);
		pushState(target, url.toString(), getState());
	}
	
	private HistoryState getState() {
		HistoryState state = new HistoryState();
		state.blobIdent = new BlobIdent(blobIdent);
		state.commentId = commentId;
		state.mark = mark;
		state.mode = mode;
		state.requestId = requestId;
		return state;
	}
	
	private void setState(HistoryState state) {
		blobIdent = new BlobIdent(state.blobIdent);
		commentId = state.commentId;
		mark = state.mark;
		mode = state.mode;
		requestId = state.requestId;
	}
	
	private void onSelect(AjaxRequestTarget target, String revision) {
		HistoryState state = getState();
		state.blobIdent.revision = revision;
		state.requestId = null;
		state.commentId = null;
		state.mode = null;
		state.mark = null;
		
		if (state.blobIdent.path != null) {
			try (	Repository repository = getDepot().openRepository();
					RevWalk revWalk = new RevWalk(repository)) {
				RevTree revTree = getDepot().getRevCommit(revision, true).getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, blobIdent.path, revTree);
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
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		Component revisionPicker = new RevisionPicker(REVISION_PICKER_ID, depotModel, blobIdent.revision, true) {

			@Override
			protected String getRevisionUrl(String revision) {
				HistoryState state = new HistoryState();
				state.blobIdent.revision = revision;
				PageParameters params = DepotFilePage.paramsOf(depotModel.getObject(), state);
				return urlFor(DepotFilePage.class, params).toString();
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				DepotFilePage.this.onSelect(target, revision);
			}
			
		}; 
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
		}
	}
	
	private void newDownloadLink(AjaxRequestTarget target) {
		ResourceLink<Void> link = new ResourceLink<Void>("download", new ArchiveResourceReference(), 
				ArchiveResource.paramsOf(getDepot(), blobIdent.revision));
		link.setOutputMarkupId(true);
		if (target != null) { 
			replace(link);
			target.add(link);
		} else {
			add(link);
		}
	}
	
	private void applyState(AjaxRequestTarget target, HistoryState state) {
		if (!state.blobIdent.revision.equals(blobIdent.revision))
			newSearchResult(target, null);
		
		setState(state);
		trait.revision = blobIdent.revision;

		newRevisionPicker(target);
		
		newDownloadLink(target);
		
		target.add(commentContext);
		
		newFileNavigator(target);
		
		target.add(revisionIndexing);
		
		newLastCommit(target);
		newFileViewer(target, null);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(ClosestDescendantResourceReference.INSTANCE));
		response.render(JavaScriptHeaderItem.forReference(CookiesResourceReference.INSTANCE));
		response.render(JQueryUIResizableJavaScriptReference.asHeaderItem());
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(DepotFilePage.class, "depot-file.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(DepotFilePage.class, "depot-file.css")));
	}

	public static PageParameters paramsOf(Depot depot, HistoryState state) {
		PageParameters params = paramsOf(depot);
		if (state.blobIdent.revision != null)
			params.set(PARAM_REVISION, state.blobIdent.revision);
		if (state.blobIdent.path != null)
			params.set(PARAM_PATH, state.blobIdent.path);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.requestId != null)
			params.set(PARAM_REQUEST, state.requestId);
		if (state.mode != null)
			params.set(PARAM_MODE, state.mode.name().toLowerCase());
		if (state.query != null)
			params.set(PARAM_QUERY, state.query);
		if (state.clientState != null)
			params.set(PARAM_CLIENT_STATE, state.clientState);
		return params;
	}
	
	private void newSearchResult(@Nullable AjaxRequestTarget target, @Nullable List<QueryHit> hits) {
		Component searchResult;
		if (hits != null) {
			searchResult = new SearchResultPanel(SEARCH_RESULD_ID, this, hits) {
				
				@Override
				protected void onClose(AjaxRequestTarget target) {
					newSearchResult(target, null);
					resizeWindow(target);
				}
				
			};
			if (target != null) {
				target.appendJavaScript(""
						+ "$('#repo-file>.search-result').show(); "
						+ "$('#repo-file .search-result>.body').focus();");
			}
		} else {
			searchResult = new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true);
			if (target != null) 
				target.appendJavaScript("$('#repo-file>.search-result').hide();");
			else 
				searchResultContainer.add(AttributeAppender.replace("style", "display: none;"));
		}
		if (target != null) {
			searchResultContainer.replace(searchResult);
			target.add(searchResult);
		} else {
			searchResultContainer.add(searchResult);
		}
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
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotFilePage.class, paramsOf(depot));
	}
	
	private static class RevisionIndexed implements WebSocketTrait {

		Long depotId;
		
		volatile String revision;
		
		@Override
		public boolean is(WebSocketTrait trait) {
			if (trait == null || !(trait instanceof RevisionIndexed))  
				return false;  
			RevisionIndexed other = (RevisionIndexed) trait;  
		    return Objects.equal(depotId, other.depotId) && Objects.equal(revision, other.revision);
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
		public void commitIndexed(Depot depot, String revision) {
			RevisionIndexed trait = new RevisionIndexed();
			trait.depotId = depot.getId();
			trait.revision = revision;
			WebSocketRenderBehavior.requestToRender(trait);
		}

		@Override
		public void indexRemoving(Depot depot) {
		}
		
	}

	@Override
	public BlobIdent getBlobIdent() {
		return blobIdent;
	}

	@Override
	public Mark getMark() {
		return mark;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable TokenPosition tokenPos) {
		Preconditions.checkArgument(blobIdent.revision.equals(this.blobIdent.revision));
		
		mark = Mark.of(tokenPos);
		if (blobIdent.equals(this.blobIdent)) {
			if (mark != null) {
				Component fileViewer = get(FILE_VIEWER_ID);
				if (fileViewer instanceof SourceViewPanel) {
					SourceViewPanel sourceViewer = (SourceViewPanel) fileViewer;
					sourceViewer.mark(target, mark);
				} else if (fileViewer instanceof FileEditPanel) {
					FileEditPanel fileEditor = (FileEditPanel) fileViewer;
					fileEditor.mark(target, mark);
				} else {
					newFileViewer(target, null);
					resizeWindow(target);
				}
			}
		} else {
			this.blobIdent = blobIdent; 
			mode = null;
			
			newFileNavigator(target);
			newLastCommit(target);
			newFileViewer(target, null);
			
			resizeWindow(target);
		}
		pushState(target);
	}

	@Override
	public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
		newSearchResult(target, hits);
		resizeWindow(target);
	}

	@Override
	public void onBlameChange(AjaxRequestTarget target, @Nullable String cmState) {
		if (mode == null)
			mode = Mode.BLAME;
		else
			mode = null;
		newFileViewer(target, cmState);
		pushState(target);
		resizeWindow(target);
	}

	@Override
	public void onDelete(AjaxRequestTarget target) {
		mode = Mode.DELETE;

		newFileViewer(target, null);
		pushState(target);
		resizeWindow(target);
	}

	@Override
	public void onEdit(AjaxRequestTarget target, @Nullable String cmState) {
		mode = Mode.EDIT;
		
		newFileNavigator(target);
		newFileViewer(target, cmState);
		pushState(target);
		resizeWindow(target);
	}

	@Override
	public boolean isOnBranch() {
		return getDepot().getRefs(Constants.R_HEADS).containsKey(blobIdent.revision);
	}

	@Override
	public boolean isAtSourceBranchHead() {
		PullRequest request = getPullRequest();
		return request != null && request.getSourceDepot() != null 
				&& blobIdent.revision.equals(request.getSource().getObjectName(false)); 
	}

	public static class HistoryState implements Serializable {
		
		private static final long serialVersionUID = 1L;

		public Long requestId;
		
		public Long commentId;
		
		public BlobIdent blobIdent = new BlobIdent();
		
		public Mark mark;
		
		public Mode mode;
		
		public transient String query;
		
		public transient String clientState;		
	}
}

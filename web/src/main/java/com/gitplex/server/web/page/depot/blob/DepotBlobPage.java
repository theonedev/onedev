package com.gitplex.server.web.page.depot.blob;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.gitplex.jsymbol.TokenPosition;
import com.gitplex.jsymbol.util.NoAntiCacheImage;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.GitPlex;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.exception.ObjectNotFoundException;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.PullRequestUpdate;
import com.gitplex.server.model.support.TextRange;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.search.IndexManager;
import com.gitplex.server.search.SearchManager;
import com.gitplex.server.search.hit.QueryHit;
import com.gitplex.server.search.query.BlobQuery;
import com.gitplex.server.search.query.TextQuery;
import com.gitplex.server.security.SecurityUtils;
import com.gitplex.server.web.PrioritizedComponentRenderer;
import com.gitplex.server.web.component.link.ArchiveMenuLink;
import com.gitplex.server.web.component.link.ViewStateAwareAjaxLink;
import com.gitplex.server.web.component.modal.ModalLink;
import com.gitplex.server.web.component.revisionpicker.RevisionPicker;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.NoBranchesPage;
import com.gitplex.server.web.page.depot.blob.navigator.BlobNavigator;
import com.gitplex.server.web.page.depot.blob.render.BlobRenderContext;
import com.gitplex.server.web.page.depot.blob.render.BlobRendererContribution;
import com.gitplex.server.web.page.depot.blob.render.view.MarkSupport;
import com.gitplex.server.web.page.depot.blob.search.advanced.AdvancedSearchPanel;
import com.gitplex.server.web.page.depot.blob.search.instant.InstantSearchPanel;
import com.gitplex.server.web.page.depot.blob.search.result.SearchResultPanel;
import com.gitplex.server.web.page.depot.commit.DepotCommitsPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes.RequestChangesPage;
import com.gitplex.server.web.page.depot.pullrequest.requestdetail.integrationpreview.IntegrationPreviewPage;
import com.gitplex.server.web.websocket.CodeCommentChangedRegion;
import com.gitplex.server.web.websocket.CommitIndexedRegion;
import com.gitplex.server.web.websocket.PullRequestChangedRegion;
import com.gitplex.server.web.websocket.WebSocketManager;
import com.gitplex.server.web.websocket.WebSocketRegion;
import com.gitplex.server.web.websocket.WebSocketRenderBehavior;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class DepotBlobPage extends DepotPage implements BlobRenderContext {

	private static final String PARAM_REVISION = "revision";
	
	private static final String PARAM_PATH = "path";
	
	private static final String PARAM_REQUEST_COMPARE_INFO = "request_compare_info";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MODE = "mode";
	
	private static final String PARAM_QUERY = "query";
	
	private static final String PARAM_MARK = "mark";
	
	private static final String PARAM_ANCHOR = "anchor";
	
	private static final String REVISION_PICKER_ID = "revisionPicker";
	
	private static final String BLOB_NAVIGATOR_ID = "blobNavigator";

	private static final String BLOB_CONTENT_ID = "blobContent";
	
	private static final String SEARCH_RESULD_ID = "searchResult";

	private State state = new State();
	
	private ObjectId resolvedRevision;
	
	private Component revisionIndexing;
	
	private WebMarkupContainer searchResultContainer;
	
	private ObjectId commitId;
	
	public DepotBlobPage(final PageParameters params) {
		super(params);
		
		if (getDepot().getDefaultBranch() == null) 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getDepot()));

		List<String> revisionAndPathSegments = new ArrayList<>();
		String segment = params.get(PARAM_REVISION).toString();
		if (segment != null && segment.length() != 0)
			revisionAndPathSegments.add(segment);
		segment = params.get(PARAM_PATH).toString();
		if (segment != null && segment.length() != 0)
			revisionAndPathSegments.add(segment);
		
		for (int i=0; i<params.getIndexedCount(); i++) {
			segment = params.get(i).toString();
			if (segment.length() != 0)
				revisionAndPathSegments.add(segment);
		}
		
		state.blobIdent = new BlobIdent(getDepot(), revisionAndPathSegments);
		
		if (state.blobIdent.revision == null)
			state.blobIdent.revision = getDepot().getDefaultBranch();

		resolvedRevision = getDepot().getObjectId(state.blobIdent.revision);
		
		RevCommit commit = getDepot().getRevCommit(state.blobIdent.revision);
		commitId = commit.copy();
		
		if (state.blobIdent.path != null) {
			try (RevWalk revWalk = new RevWalk(getDepot().getRepository())) {
				RevTree revTree = commit.getTree();
				TreeWalk treeWalk = TreeWalk.forPath(getDepot().getRepository(), state.blobIdent.path, revTree);
				if (treeWalk == null) {
					throw new ObjectNotFoundException("Unable to find blob path '" + state.blobIdent.path
							+ "' in revision '" + state.blobIdent.revision + "'");
				}
				state.blobIdent.mode = treeWalk.getRawMode(0);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			state.blobIdent.mode = FileMode.TREE.getBits();
		}
		
		String modeStr = params.get(PARAM_MODE).toString();
		if (modeStr != null)
			state.mode = Mode.valueOf(modeStr.toUpperCase());
		
		state.mark = TextRange.of(params.get(PARAM_MARK).toString());
		state.anchor = params.get(PARAM_ANCHOR).toString();
		
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		
		state.requestCompareInfo = RequestCompareInfo.fromString(params.get(PARAM_REQUEST_COMPARE_INFO).toString());
		
		state.query = params.get(PARAM_QUERY).toString();
		
		if (state.mode == Mode.ADD || state.mode == Mode.EDIT || state.mode == Mode.DELETE) {
			if (!isOnBranch()) 
				throw new IllegalArgumentException("Files can only be edited on branch");
			
			String path = state.blobIdent.path;
			if (path != null && state.blobIdent.isTree())
				path += "/";
			if (!SecurityUtils.canModify(getDepot(), state.blobIdent.revision, path))
				unauthorized();
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		newRevisionPicker(null);
		newBlobNavigator(null);
		newBlobOperations(null);
		
		add(revisionIndexing = new WebMarkupContainer("revisionIndexing") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new NoAntiCacheImage("icon", new PackageResourceReference(DepotBlobPage.class, "indexing.gif")));
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				IndexManager indexManager = GitPlex.getInstance(IndexManager.class);
				if (!indexManager.isIndexed(getDepot(), commitId)) {
					GitPlex.getInstance(IndexManager.class).indexAsync(getDepot(), commitId);
					setVisible(true);
				} else {
					setVisible(false);
				}
			}
			
		});

		newBlobContent(null);

		add(searchResultContainer = new WebMarkupContainer("searchResultContainer"));

		List<QueryHit> queryHits;
		if (state.query != null) {
			BlobQuery query = new TextQuery.Builder()
					.term(state.query)
					.wholeWord(true)
					.caseSensitive(true) 
					.count(SearchResultPanel.MAX_QUERY_ENTRIES)
					.build();
			try {
				SearchManager searchManager = GitPlex.getInstance(SearchManager.class);
				queryHits = searchManager.search(depotModel.getObject(), commitId, query);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}								
		} else {
			queryHits = null;
		}
		
		newSearchResult(null, queryHits);
		
		add(new WebSocketRenderBehavior() {
			
			@Override
			protected void onRender(WebSocketRequestHandler handler) {
				handler.add(revisionIndexing);
				resizeWindow(handler);
			}
			
		});
	}

	/*
	 * In case we are on a branch, this operation makes sure that the branch resolves
	 * to a certain commit during the life cycle of our page, unless the page is 
	 * refreshed. This can avoid the issue that displayed file content and subsequent 
	 * operations encounters different commit if someone commits to the branch while 
	 * we are staying on the page. 
	 */
	@Override
	protected Map<String, ObjectId> getObjectIdCache() {
		Map<String, ObjectId> objectIdCache = new HashMap<>();
		if (resolvedRevision != null)
			objectIdCache.put(state.blobIdent.revision, resolvedRevision);
		return objectIdCache;
	}

	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return GitPlex.getInstance(CodeCommentManager.class).load(state.commentId);
		else
			return null;
	}
	
	private void newBlobNavigator(@Nullable AjaxRequestTarget target) {
		Component blobNavigator = new BlobNavigator(BLOB_NAVIGATOR_ID, this);
		if (target != null) {
			replace(blobNavigator);
			target.add(blobNavigator);
		} else {
			add(blobNavigator);
		}
	}

	private void newBlobOperations(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer blobOperations = new WebMarkupContainer("blobOperations");
		blobOperations.setOutputMarkupId(true);
		
		blobOperations.add(new ViewStateAwareAjaxLink<Void>("add") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onModeChange(target, Mode.ADD);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible((state.mode == Mode.VIEW || state.mode == Mode.BLAME) 
						&& isOnBranch() 
						&& state.blobIdent.isTree()
						&& SecurityUtils.canModify(getDepot(), state.blobIdent.revision, state.blobIdent.path));
			}
			
		});
		
		blobOperations.add(new InstantSearchPanel("instantSearch", depotModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return state.blobIdent.revision;
			}
			
		}) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, QueryHit hit) {
				BlobIdent selected = new BlobIdent(state.blobIdent.revision, hit.getBlobPath(), 
						FileMode.REGULAR_FILE.getBits()); 
				DepotBlobPage.this.onSelect(target, selected, hit.getTokenPos());
			}
			
			@Override
			protected void onMoreQueried(AjaxRequestTarget target, List<QueryHit> hits) {
				newSearchResult(target, hits);
				resizeWindow(target);
			}
			
		});
		
		blobOperations.add(new ModalLink("advancedSearch") {

			@Override
			protected Component newContent(String id) {
				return new AdvancedSearchPanel(id, depotModel, new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return state.blobIdent.revision;
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
						return state.blobIdent;
					}
					
				};
			}
			
		});

		blobOperations.add(new Link<Void>("history") {

			@Override
			public void onClick() {
				DepotCommitsPage.State state = new DepotCommitsPage.State();
				state.setCompareWith(resolvedRevision.name());
				if (DepotBlobPage.this.state.blobIdent.path != null) 
					state.setQuery(String.format("path(%s)", DepotBlobPage.this.state.blobIdent.path));
				setResponsePage(DepotCommitsPage.class, DepotCommitsPage.paramsOf(getDepot(), state));				
			}
			
		});
		
		blobOperations.add(new ArchiveMenuLink("download", depotModel) {

			@Override
			protected String getRevision() {
				return state.blobIdent.revision;
			}

		});
		
		if (target != null) {
			replace(blobOperations);
			target.add(blobOperations);
		} else {
			add(blobOperations);
		}
	}
	
	private void newBlobContent(@Nullable AjaxRequestTarget target) {
		PrioritizedComponentRenderer mostPrioritizedRenderer = null;
		for (BlobRendererContribution contribution: GitPlex.getExtensions(BlobRendererContribution.class)) {
			PrioritizedComponentRenderer renderer = contribution.getRenderer(this);
			if (renderer != null) {
				if (mostPrioritizedRenderer == null || mostPrioritizedRenderer.getPriority() > renderer.getPriority())
					mostPrioritizedRenderer = renderer;
			}
		}
		Component blobContent = Preconditions.checkNotNull(mostPrioritizedRenderer).render(BLOB_CONTENT_ID);
		if (target != null) {
			replace(blobContent);
			target.add(blobContent);
		} else {
			add(blobContent);
		}
	}
	
	private void showRequestChanges(AjaxRequestTarget target, ObjectId oldCommitId, ObjectId newCommitId) {
		if (state.requestCompareInfo.compareState != null) {
			RequestChangesPage.State changeState = SerializationUtils.clone(this.state.requestCompareInfo.compareState);
			changeState.newCommit = newCommitId.name();
			changeState.pathFilter = null;
			if (changeState.commentId != null) {
				CodeComment comment = GitPlex.getInstance(CodeCommentManager.class).load(changeState.commentId);
				changeState.oldCommit = comment.getCommentPos().getCommit();
			} else {
				changeState.oldCommit = oldCommitId.name();
			}
			get(BLOB_CONTENT_ID).add(new WebSocketRenderBehavior() {
				
				@Override
				protected void onRender(WebSocketRequestHandler handler) {
					PullRequest request = GitPlex.getInstance(PullRequestManager.class)
							.load(DepotBlobPage.this.state.requestCompareInfo.requestId);
					for (PullRequestUpdate update: request.getUpdates()) {
						if (update.getHeadCommitHash().equals(newCommitId.name())) {
							setResponsePage(RequestChangesPage.class, RequestChangesPage.paramsOf(request, changeState));
							break;
						}
					}
				}
				
			});
		} else {
			get(BLOB_CONTENT_ID).add(new WebSocketRenderBehavior() {
				
				@Override
				protected void onRender(WebSocketRequestHandler handler) {
					PullRequest request = GitPlex.getInstance(PullRequestManager.class)
							.load(state.requestCompareInfo.requestId);
					for (PullRequestUpdate update: request.getUpdates()) {
						if (update.getHeadCommitHash().equals(newCommitId.name())) {
							setResponsePage(RequestChangesPage.class, IntegrationPreviewPage.paramsOf(request));
							break;
						}
					}
				}
				
			});
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), state);
		CharSequence url = RequestCycle.get().urlFor(DepotBlobPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	private void newRevisionPicker(@Nullable AjaxRequestTarget target) {
		Component revisionPicker = new RevisionPicker(REVISION_PICKER_ID, depotModel, state.blobIdent.revision, true) {

			@Override
			protected String getRevisionUrl(String revision) {
				State state = new State();
				state.blobIdent.revision = revision;
				PageParameters params = DepotBlobPage.paramsOf(depotModel.getObject(), state);
				return urlFor(DepotBlobPage.class, params).toString();
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, String revision) {
				BlobIdent newBlobIdent = new BlobIdent();
				newBlobIdent.revision = revision;
				if (state.blobIdent.path != null) {
					try (RevWalk revWalk = new RevWalk(getDepot().getRepository())) {
						RevTree revTree = getDepot().getRevCommit(revision, true).getTree();
						TreeWalk treeWalk = TreeWalk.forPath(getDepot().getRepository(), state.blobIdent.path, revTree);
						if (treeWalk != null) {
							newBlobIdent.path = state.blobIdent.path;
							newBlobIdent.mode = treeWalk.getRawMode(0);
						} else {
							newBlobIdent.mode = FileMode.TREE.getBits();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				
				DepotBlobPage.this.onSelect(target, newBlobIdent, null);
			}
			
		}; 
		if (target != null) {
			replace(revisionPicker);
			target.add(revisionPicker);
		} else {
			add(revisionPicker);
		}
	}
	
	/*
	 * This method represents changing of resolved revision, instead of the named revision. 
	 * It is possible that resolved revision changes while named revision remains unchanged. 
	 * For instance when a file on a branch is edited from this page, the named revision 
	 * remains unchanged (still point to the branch), but the underlying resolved revision 
	 * (the commit) has been changed 
	 *   
	 * @param target
	 */
	private void onResolvedRevisionChange(AjaxRequestTarget target) {
		/* 
		 * A hack to reset resolved revision to null to disable getObjectIdCache()
		 * temporarily as otherwise getObjectId() method below will always 
		 * resolved to existing value of resolvedRevision
		 */
		resolvedRevision = null;
		resolvedRevision = getDepot().getObjectId(state.blobIdent.revision);
		commitId = getDepot().getRevCommit(state.blobIdent.revision).copy();
		GitPlex.getInstance(WebSocketManager.class).onRegionChange(this);
		newRevisionPicker(target);
		target.add(revisionIndexing);
		newBlobNavigator(target);
		newBlobOperations(target);
		newBlobContent(target);
		resizeWindow(target);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new DepotBlobResourceReference()));
	}

	public static PageParameters paramsOf(Depot depot, CodeComment comment, @Nullable String anchor) {
		DepotBlobPage.State state = new DepotBlobPage.State();
		state.blobIdent.revision = comment.getCommentPos().getCommit();
		state.blobIdent.path = comment.getCommentPos().getPath();
		state.blobIdent.mode = FileMode.TYPE_FILE;
		state.commentId = comment.getId();
		state.mark = comment.getCommentPos().getRange();
		state.anchor = anchor;
		return paramsOf(depot, state);
	}
	
	public static PageParameters paramsOf(Depot depot, State state) {
		PageParameters params = paramsOf(depot);
		if (state.blobIdent.revision != null)
			params.set(PARAM_REVISION, state.blobIdent.revision);
		if (state.blobIdent.path != null)
			params.set(PARAM_PATH, state.blobIdent.path);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		if (state.anchor != null)
			params.set(PARAM_ANCHOR, state.anchor);
		if (state.requestCompareInfo != null)
			params.set(PARAM_REQUEST_COMPARE_INFO, state.requestCompareInfo.toString());
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mode != Mode.VIEW)
			params.set(PARAM_MODE, state.mode.name().toLowerCase());
		if (state.query != null)
			params.set(PARAM_QUERY, state.query);
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
						+ "$('#depot-blob>.search-result').show(); "
						+ "$('#depot-blob .search-result>.body').focus();");
			}
		} else {
			searchResult = new WebMarkupContainer(SEARCH_RESULD_ID).setOutputMarkupId(true);
			if (target != null) 
				target.appendJavaScript("$('#depot-blob>.search-result').hide();");
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

		State popState = (State) data;
		if (!popState.blobIdent.revision.equals(state.blobIdent.revision)) {
			state = popState;
			newSearchResult(target, null);
			onResolvedRevisionChange(target);
		} else {
			state = popState;
			newBlobNavigator(target);
			newBlobOperations(target);
			newBlobContent(target);
			resizeWindow(target);
		}
	}
	
	private void resizeWindow(IPartialPageRequestHandler partialPageRequestHandler) {
		partialPageRequestHandler.appendJavaScript("$(window).resize();");
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotBlobPage.class, paramsOf(depot));
	}
	
	@Override
	public BlobIdent getBlobIdent() {
		return state.blobIdent;
	}

	@Override
	public TextRange getMark() {
		return state.mark;
	}

	@Override
	public String getAnchor() {
		return state.anchor;
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, TextRange mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public String getMarkUrl(TextRange mark) {
		State markState = SerializationUtils.clone(state);
		markState.blobIdent.revision = resolvedRevision.name();
		markState.requestCompareInfo = null;
		markState.commentId = null;
		markState.mark = mark;
		PageParameters params = paramsOf(getDepot(), markState);		
		return RequestCycle.get().urlFor(DepotBlobPage.class, params).toString();
	}
	
	@Override
	public Mode getMode() {
		return state.mode;
	}

	@Override
	public void onSelect(AjaxRequestTarget target, BlobIdent blobIdent, @Nullable TokenPosition tokenPos) {
		state.mark = TextRange.of(tokenPos);
		if (!blobIdent.revision.equals(state.blobIdent.revision)) {
			state.blobIdent = blobIdent;
			state.mode = Mode.VIEW;
			state.commentId = null;
			newSearchResult(target, null);
			onResolvedRevisionChange(target);
		} else {
			if (!Objects.equal(state.blobIdent.path, blobIdent.path) 
					|| state.mark != null && !(get(BLOB_CONTENT_ID) instanceof MarkSupport)) {
				state.blobIdent.path = blobIdent.path;
				state.blobIdent.mode = blobIdent.mode;
				state.mode = Mode.VIEW;
				state.commentId = null;
				newBlobNavigator(target);
				newBlobOperations(target);
				newBlobContent(target);
				resizeWindow(target);
				GitPlex.getInstance(WebSocketManager.class).onRegionChange(this);
			} else if (state.mark != null) {
				// This logic is added for performance reason, we do not want to 
				// reload the file if go to different mark positions in same file
				((MarkSupport)get(BLOB_CONTENT_ID)).mark(target, state.mark);
			}
		}
		pushState(target);
	}

	@Override
	public void onSearchComplete(AjaxRequestTarget target, List<QueryHit> hits) {
		newSearchResult(target, hits);
		resizeWindow(target);
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			state.commentId = comment.getId();
			state.mark = comment.getCommentPos().getRange();
		} else {
			state.commentId = null;
			state.mark = null;
		}
		GitPlex.getInstance(WebSocketManager.class).onRegionChange(this);
		pushState(target);
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, TextRange mark) {
		state.commentId = null;
		GitPlex.getInstance(WebSocketManager.class).onRegionChange(this);
		state.mark = mark;
		pushState(target);
	}
	
	@Override
	public boolean isOnBranch() {
		return getDepot().getBranchRef(state.blobIdent.revision) != null;
	}

	@Override
	public RevCommit getCommit() {
		return getDepot().getRevCommit(getBlobIdent().revision);
	}
	
	@Override
	protected boolean isFootVisible() {
		return false;
	}

	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		regions.add(new CommitIndexedRegion(getDepot().getId(), commitId));
		if (state.commentId != null)
			regions.add(new CodeCommentChangedRegion(state.commentId));
		
		/*
		 * add below so that we can jump to request changes page after editing a file 
		 */
		if (state.requestCompareInfo != null)
			regions.add(new PullRequestChangedRegion(state.requestCompareInfo.requestId));
		return regions;
	}

	public static class State implements Serializable {
		
		private static final long serialVersionUID = 1L;

		public Long commentId;
		
		public BlobIdent blobIdent = new BlobIdent();
		
		public TextRange mark;
		
		public String anchor;
		
		public Mode mode = Mode.VIEW;
		
		public RequestCompareInfo requestCompareInfo;
		
		public String query;
		
	}

	@Override
	public void onModeChange(AjaxRequestTarget target, Mode mode) {
		if (state.mode == Mode.EDIT && mode == Mode.VIEW && state.requestCompareInfo != null) {
			PullRequest request = GitPlex.getInstance(PullRequestManager.class)
					.load(state.requestCompareInfo.requestId);
			if (state.requestCompareInfo.compareState != null) {
				PageParameters params = RequestChangesPage.paramsOf(request, 
						state.requestCompareInfo.compareState);
				setResponsePage(RequestChangesPage.class, params);
			} else {
				PageParameters params = IntegrationPreviewPage.paramsOf(request, 
						state.requestCompareInfo.previewState);
				setResponsePage(IntegrationPreviewPage.class, params);
			}
		} else {
			/*
			 * User might be changing blob name when adding a file, and onModeChange will be called. 
			 * In this case, we only need to re-create blob content
			 */
			if (mode != Mode.ADD || state.mode != Mode.ADD) {
				state.mode = mode;
				pushState(target);
				if (state.mode == Mode.VIEW || state.mode == Mode.EDIT || state.mode == Mode.ADD) {
					newBlobNavigator(target);
					newBlobOperations(target);
				}
			}			
			newBlobContent(target);
			resizeWindow(target);
		}
	}

	@Override
	public void onCommitted(AjaxRequestTarget target, ObjectId oldCommit, ObjectId newCommit) {
		String refName = GitUtils.branch2ref(state.blobIdent.revision);
		BlobIdent newBlobIdent;
		if (state.mode == Mode.DELETE) {
			Depot depot = getDepot();
			String branch = state.blobIdent.revision;
			depot.cacheObjectId(branch, newCommit);
			resolvedRevision = newCommit;
			try (RevWalk revWalk = new RevWalk(getDepot().getRepository())) {
				RevTree revTree = getDepot().getRevCommit(newCommit).getTree();
				String parentPath = StringUtils.substringBeforeLast(state.blobIdent.path, "/");
				while (TreeWalk.forPath(getDepot().getRepository(), parentPath, revTree) == null) {
					if (parentPath.contains("/")) {
						parentPath = StringUtils.substringBeforeLast(parentPath, "/");
					} else {
						parentPath = null;
						break;
					}
				}

				Long depotId = depot.getId();
				Subject subject = SecurityUtils.getSubject();
				ObjectId oldCommitId = oldCommit.copy();
				ObjectId newCommitId = newCommit.copy();
				GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

					@Override
					public void run() {
						ThreadContext.bind(subject);
						try {
							Depot depot = GitPlex.getInstance(DepotManager.class).load(depotId);
							depot.cacheObjectId(branch, newCommitId);
							RefUpdated event = new RefUpdated(depot, refName, oldCommitId, newCommitId);
							GitPlex.getInstance(ListenerRegistry.class).post(event);
						} finally {
							ThreadContext.unbindSubject();
						}
					}
					
				});
				
				newBlobIdent = new BlobIdent(branch, parentPath, FileMode.TREE.getBits());
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}	
		} else {
			Depot depot = getDepot();
			String branch = state.blobIdent.revision;
			depot.cacheObjectId(branch, newCommit);
			resolvedRevision = newCommit;
			newBlobIdent = new BlobIdent(branch, getNewPath(), FileMode.REGULAR_FILE.getBits());
			
			Subject subject = SecurityUtils.getSubject();
			
			Long depotId = depot.getId();
			ObjectId oldCommitId = oldCommit.copy();
			ObjectId newCommitId = newCommit.copy();
			GitPlex.getInstance(UnitOfWork.class).doAsync(new Runnable() {

				@Override
				public void run() {
					ThreadContext.bind(subject);
					try {
						Depot depot = GitPlex.getInstance(DepotManager.class).load(depotId);
						depot.cacheObjectId(branch, oldCommitId);
						RefUpdated event = new RefUpdated(depot, refName, oldCommitId, newCommitId);
						GitPlex.getInstance(ListenerRegistry.class).post(event);
					} finally {
						ThreadContext.unbindSubject();
					}
				}
				
			});
		}
		
		if (state.requestCompareInfo != null) {
			showRequestChanges(target, oldCommit, newCommit);
		} else {
			state.blobIdent = newBlobIdent;
			state.mark = null;
			state.commentId = null;
			state.mode = Mode.VIEW;
			onResolvedRevisionChange(target);
			pushState(target);
		}

		// fix the issue that sometimes indexing indicator of new commit does not disappear 
		target.appendJavaScript("Wicket.WebSocket.send('RenderCallback');");	    			
	}

	@Override
	public String getNewPath() {
		BlobNavigator blobNavigator = (BlobNavigator) get(BLOB_NAVIGATOR_ID);
		return blobNavigator.getNewPath();
	}
	
}

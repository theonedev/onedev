package com.gitplex.server.web.page.depot.commit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.BlobIdent;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.RefInfo;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.CommentPos;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.behavior.clipboard.CopyClipboardBehavior;
import com.gitplex.server.web.component.avatar.ContributorAvatars;
import com.gitplex.server.web.component.commitmessage.CommitMessageLabel;
import com.gitplex.server.web.component.contributorpanel.ContributorPanel;
import com.gitplex.server.web.component.createbranch.CreateBranchLink;
import com.gitplex.server.web.component.createtag.CreateTagLink;
import com.gitplex.server.web.component.diff.revision.CommentSupport;
import com.gitplex.server.web.component.diff.revision.RevisionDiffPanel;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.page.depot.DepotPage;
import com.gitplex.server.web.page.depot.blob.DepotBlobPage;
import com.gitplex.server.web.page.depot.branches.DepotBranchesPage;
import com.gitplex.server.web.page.depot.tags.DepotTagsPage;
import com.gitplex.server.web.websocket.CommitIndexedRegion;
import com.gitplex.server.web.websocket.WebSocketRegion;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class CommitDetailPage extends DepotPage implements CommentSupport {

	private static final String PARAM_REVISION = "revision";
	
	// make sure to use a different value from wicket:id according to wicket bug:
	// https://issues.apache.org/jira/browse/WICKET-6069
	private static final String PARAM_COMPARE_WITH = "compare-with";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private State state;
	
	private ObjectId resolvedRevision;
	
	private ObjectId resolvedCompareWith;
	
	private RevisionDiffPanel revisionDiff;
	
	public CommitDetailPage(PageParameters params) {
		super(params);
		
		state = new State(params);
		resolvedRevision = getDepot().getRevCommit(state.revision).copy();
		if (state.compareWith != null)
			resolvedCompareWith = getDepot().getRevCommit(state.compareWith).copy();
	}

	private RevCommit getCommit() {
		return getDepot().getRevCommit(state.revision);
	}
	
	private List<RevCommit> getParents() {
		List<RevCommit> parents = new ArrayList<>();
		for (RevCommit parent: getCommit().getParents())
			parents.add(parent);
		return parents;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new CommitMessageLabel("text", depotModel, Model.of(getCommit().getShortMessage())));

		BlobIdent blobIdent = new BlobIdent(getCommit().name(), null, FileMode.TYPE_TREE);
		DepotBlobPage.State browseState = new DepotBlobPage.State(blobIdent);
		PageParameters params = DepotBlobPage.paramsOf(depotModel.getObject(), browseState);
		add(new ViewStateAwarePageLink<Void>("browseCode", DepotBlobPage.class, params));
		
		add(new CreateBranchLink("createBranch", depotModel, state.revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String branch) {
				setResponsePage(DepotBranchesPage.class, DepotBranchesPage.paramsOf(getDepot()));
			}
			
		});
		
		add(new CreateTagLink("createTag", depotModel, state.revision) {

			@Override
			protected void onCreate(AjaxRequestTarget target, String tag) {
				setResponsePage(DepotTagsPage.class, DepotTagsPage.paramsOf(getDepot()));
			}
			
		});
		
		String message = GitUtils.getDetailMessage(getCommit());
		if (message != null) {
			add(new CommitMessageLabel("detail", depotModel, Model.of(message)));
		} else {
			add(new WebMarkupContainer("detail").setVisible(false));
		}
		
		add(new AjaxLazyLoadPanel("refs") {

			@Override
			public Component getLazyLoadComponent(String markupId) {
				Fragment fragment = new Fragment(markupId, "refsFrag", CommitDetailPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script = String.format("gitplex.server.commitdetail.initRefs('%s');", getMarkupId());
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				fragment.add(new ListView<RefInfo>("refs", new LoadableDetachableModel<List<RefInfo>>() {

					@Override
					protected List<RefInfo> load() {
						Set<ObjectId> descendants = GitPlex.getInstance(CommitInfoManager.class)
								.getDescendants(getDepot(), getCommit().getId());
						descendants.add(getCommit().getId());
					
						List<RefInfo> refs = new ArrayList<>();
						refs.addAll(getDepot().getBranches());
						refs.addAll(getDepot().getTags());
						return refs.stream().filter(ref->descendants.contains(ref.getPeeledObj())).collect(Collectors.toList());
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<RefInfo> item) {
						String ref = item.getModelObject().getRef().getName();
						String branch = GitUtils.ref2branch(ref); 
						if (branch != null) {
							BlobIdent blobIdent = new BlobIdent(branch, null, FileMode.TREE.getBits());
							DepotBlobPage.State state = new DepotBlobPage.State(blobIdent);
							Link<Void> link = new ViewStateAwarePageLink<Void>("link", DepotBlobPage.class, 
									DepotBlobPage.paramsOf(depotModel.getObject(), state));
							link.add(new Label("label", branch));
							item.add(link);
							item.add(AttributeAppender.append("class", "branch"));
						} else {
							String tag = Preconditions.checkNotNull(GitUtils.ref2tag(ref));
							BlobIdent blobIdent = new BlobIdent(tag, null, FileMode.TREE.getBits());
							DepotBlobPage.State state = new DepotBlobPage.State(blobIdent);
							Link<Void> link = new ViewStateAwarePageLink<Void>("link", DepotBlobPage.class, 
									DepotBlobPage.paramsOf(depotModel.getObject(), state));
							link.add(new Label("label", tag));
							item.add(link);
							item.add(AttributeAppender.append("class", "tag"));
						}
					}
					
				});
				return fragment;
			}
			
		});
		
		add(new ContributorAvatars("contributorAvatars", getCommit().getAuthorIdent(), getCommit().getCommitterIdent()));
		add(new ContributorPanel("contribution", getCommit().getAuthorIdent(), getCommit().getCommitterIdent(), true));

		add(new Label("hash", GitUtils.abbreviateSHA(getCommit().name())));
		add(new WebMarkupContainer("copyHash").add(new CopyClipboardBehavior(Model.of(getCommit().name()))));
		
		newParentsContainer(null);

		if (getCommit().getParentCount() != 0) {
			newRevisionDiff(null);
		} else {
			add(new WebMarkupContainer("revisionDiff").setVisible(false));
		}
	}
	
	private void newParentsContainer(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer parentsContainer = new WebMarkupContainer("parents");
		parentsContainer.setOutputMarkupId(true);
		if (getParents().size() == 1) {
			RevCommit parent = getParents().get(0);
			State newState = new State();
			newState.revision = parent.name();
			newState.whitespaceOption = state.whitespaceOption;
			newState.pathFilter = state.pathFilter;
			Link<Void> link = new ViewStateAwarePageLink<Void>("parent", CommitDetailPage.class, 
					paramsOf(depotModel.getObject(), newState));
			link.add(new Label("label", GitUtils.abbreviateSHA(parent.name())));
			parentsContainer.add(link);
			parentsContainer.add(new WebMarkupContainer("parents").setVisible(false));
		} else {
			parentsContainer.add(new WebMarkupContainer("parent").setVisible(false));
			parentsContainer.add(new Label("count", getParents().size() + " parents"));
			parentsContainer.add(new ListView<RevCommit>("parents", new LoadableDetachableModel<List<RevCommit>>() {

				@Override
				protected List<RevCommit> load() {
					return getParents();
				}
				
			}) {

				@Override
				protected void populateItem(ListItem<RevCommit> item) {
					RevCommit parent = item.getModelObject();

					State newState = new State();
					newState.revision = parent.name();
					newState.whitespaceOption = state.whitespaceOption;
					newState.pathFilter = state.pathFilter;
					
					Link<Void> link = new ViewStateAwarePageLink<Void>("link", CommitDetailPage.class, 
							paramsOf(depotModel.getObject(), newState));
					link.add(new Label("label", GitUtils.abbreviateSHA(parent.name())));
					item.add(link);
					
					item.add(new AjaxLink<Void>("diff") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							state.compareWith = item.getModelObject().name();
							resolvedCompareWith = item.getModelObject().copy();
							
							target.add(parentsContainer);
							newRevisionDiff(target);
							pushState(target);
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							if (item.getModelObject().equals(getCompareWith())) 
								add(AttributeAppender.append("class", "active"));
						}	
						
					});
				}
				
			});
		}		
		if (target != null) {
			replace(parentsContainer);
			target.add(parentsContainer);
		} else {
			add(parentsContainer);
		}
	}
	
	private ObjectId getCompareWith() {
		List<RevCommit> parents = getParents();
		if (resolvedCompareWith != null) {
			if (parents.contains(resolvedCompareWith)) 
				return resolvedCompareWith;
			else
				return parents.get(0);
		} else {
			return parents.get(0);
		}
	}
	
	private void newRevisionDiff(@Nullable AjaxRequestTarget target) {
		IModel<String> blameModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.blameFile;
			}

			@Override
			public void setObject(String object) {
				state.blameFile = object;
				pushState(RequestCycle.get().find(AjaxRequestTarget.class));
			}
			
		};
		IModel<String> pathFilterModel = new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.pathFilter;
			}

			@Override
			public void setObject(String object) {
				state.pathFilter = object;
				pushState(RequestCycle.get().find(AjaxRequestTarget.class));
			}
			
		};
		IModel<WhitespaceOption> whitespaceOptionModel = new IModel<WhitespaceOption>() {

			@Override
			public void detach() {
			}

			@Override
			public WhitespaceOption getObject() {
				return state.whitespaceOption;
			}

			@Override
			public void setObject(WhitespaceOption object) {
				state.whitespaceOption = object;
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				newParentsContainer(target);
				pushState(target);
			}
			
		};
		revisionDiff = new RevisionDiffPanel("revisionDiff", depotModel,  
				Model.of((PullRequest)null), getCompareWith().name(), state.revision, 
				pathFilterModel, whitespaceOptionModel, blameModel, this);
		revisionDiff.setOutputMarkupId(true);
		if (target != null) {
			replace(revisionDiff);
			target.add(revisionDiff);
		} else {
			add(revisionDiff);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new CommitDetailResourceReference()));
	}

	public static PageParameters paramsOf(Depot depot, State state) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_REVISION, state.revision);
		if (state.compareWith != null)
			params.set(PARAM_COMPARE_WITH, state.compareWith);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.set(PARAM_BLAME_FILE, state.blameFile);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		return params;
	}
	
	public static PageParameters paramsOf(Depot depot, String revision) {
		State state = new State();
		state.revision = revision;
		return paramsOf(depot, state);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(DepotCommitsPage.class, paramsOf(depot));
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), state);
		CharSequence url = RequestCycle.get().urlFor(CommitDetailPage.class, params);
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (State) data;
		newRevisionDiff(target);
	}
	
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String revision;
		
		@Nullable
		public String compareWith;
		
		@Nullable
		public Long commentId;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public CommentPos mark;
		
		public State() {
		}
		
		public State(PageParameters params) {
			List<String> revisionSegments = new ArrayList<>();
			String segment = params.get(PARAM_REVISION).toString();
			if (segment.length() != 0)
				revisionSegments.add(segment);
			for (int i=0; i<params.getIndexedCount(); i++) {
				segment = params.get(i).toString();
				if (segment.length() != 0)
					revisionSegments.add(segment);
			}
			
			revision = Joiner.on("/").join(revisionSegments);
			compareWith = params.get(PARAM_COMPARE_WITH).toString();
			whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
			pathFilter = params.get(PARAM_PATH_FILTER).toString();
			blameFile = params.get(PARAM_BLAME_FILE).toString();
			commentId = params.get(PARAM_COMMENT).toOptionalLong();
			mark = CommentPos.fromString(params.get(PARAM_MARK).toString());
		}
		
	}

	@Override
	public CommentPos getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(CommentPos mark) {
		State markState = new State();
		markState.mark = mark;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWith = state.compareWith;
		markState.pathFilter = state.pathFilter;
		markState.revision = resolvedRevision.name();
		return urlFor(CommitDetailPage.class, paramsOf(getDepot(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		String compareWith = getCompareWith().name();
		commentState.mark = comment.getCommentPos();
		commentState.commentId = comment.getId();
		commentState.whitespaceOption = state.whitespaceOption;
		commentState.compareWith = compareWith;
		commentState.pathFilter = state.pathFilter;
		commentState.revision = resolvedRevision.name();
		return urlFor(CommitDetailPage.class, paramsOf(getDepot(), commentState)).toString();
	}
	
	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return GitPlex.getInstance(CodeCommentManager.class).load(state.commentId);
		else
			return null;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			state.commentId = comment.getId();
			state.mark = comment.getCommentPos();
		} else {
			state.commentId = null;
			state.mark = null;
		}
		pushState(target);
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, CommentPos mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, CommentPos mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
	}
	
	@Override
	protected Map<String, ObjectId> getObjectIdCache() {
		Map<String, ObjectId> objectIdCache = new HashMap<>();
		objectIdCache.put(state.revision, resolvedRevision);
		if (state.compareWith != null)
			objectIdCache.put(state.compareWith, resolvedCompareWith);
		return objectIdCache;
	}

	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		regions.add(new CommitIndexedRegion(getDepot().getId(), resolvedRevision));
		regions.add(new CommitIndexedRegion(getDepot().getId(), getCompareWith()));
		return regions;
	}

}

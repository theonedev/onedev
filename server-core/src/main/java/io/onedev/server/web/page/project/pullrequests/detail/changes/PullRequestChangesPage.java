package io.onedev.server.web.page.project.pullrequests.detail.changes;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestApproveData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestReopenData;
import io.onedev.server.model.support.pullrequest.changedata.PullRequestRequestedForChangesData;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.diff.revision.CommentSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.floating.AlignPlacement;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.util.EditParamsAware;
import io.onedev.server.web.websocket.WebSocketManager;

@SuppressWarnings("serial")
public class PullRequestChangesPage extends PullRequestDetailPage implements CommentSupport, EditParamsAware {

	public static final String PARAM_OLD_COMMIT = "old-commit";
	
	public static final String PARAM_NEW_COMMIT = "new-commit";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	public static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private State state = new State();
	
	private WebMarkupContainer head;
	
	private final IModel<List<RevCommit>> commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

		@Override
		protected List<RevCommit> load() {
			List<RevCommit> commits = new ArrayList<>();
			for (PullRequestUpdate update: getPullRequest().getSortedUpdates()) {
				commits.addAll(update.getCommits());
			}
			return commits;
		}
		
	};
	
	private final IModel<ObjectId> comparisonBaseModel = new LoadableDetachableModel<ObjectId>() {
		
		@Override
		protected ObjectId load() {
			ObjectId oldCommitId = ObjectId.fromString(state.oldCommitHash);
			ObjectId newCommitId = ObjectId.fromString(state.newCommitHash);
			return getPullRequest().getComparisonBase(oldCommitId, newCommitId);
		}
		
	};
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			Collection<CodeComment> comments = new ArrayList<>();
			for (CodeComment comment: getPullRequest().getCodeComments()) {
				Mark diffMark = getDiffMark(comment.getMark());
				if (diffMark != null) {
					comment.setMark(diffMark);
					comments.add(comment);
				}
			}
			return comments;
		}
		
	};
	
	private final IModel<CodeComment> openCommentModel = new LoadableDetachableModel<CodeComment>() {

		@Override
		protected CodeComment load() {
			if (state.commentId != null) {
				CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
				Mark diffMark = getDiffMark(comment.getMark());
				if (diffMark != null) {
					comment.setMark(diffMark);
					return comment;
				}
			} 
			return null;
		}
		
	};
	
	public PullRequestChangesPage(PageParameters params) {
		super(params);

		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = Mark.fromString(params.get(PARAM_MARK).toString());
		
		state.oldCommitHash = params.get(PARAM_OLD_COMMIT).toString();
		state.newCommitHash = params.get(PARAM_NEW_COMMIT).toString();
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
		     
		PullRequest request = getPullRequest();
		if (state.commentId != null) {
			CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
			Preconditions.checkState(comment.getRequest().equals(request));
			
			CodeComment.ComparingInfo commentComparingInfo = comment.getComparingInfo();
			PullRequest.ComparingInfo requestComparingInfo = 
					getPullRequest().getRequestComparingInfo(commentComparingInfo);
			if (requestComparingInfo != null && state.oldCommitHash == null && state.newCommitHash == null) {
				if (comment.isContextChanged(request)) {
					state.oldCommitHash = comment.getMark().getCommitHash();
					state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
				} else {
					state.oldCommitHash = requestComparingInfo.getOldCommitHash();
					state.newCommitHash = requestComparingInfo.getNewCommitHash();
				}
			} 
		} 
		if (state.oldCommitHash == null) 
			state.oldCommitHash = request.getBaseCommitHash();
		if (state.newCommitHash == null)
			state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
	}
	
	private int getCommitIndex(String commitHash) {
		int index = -1;
		for (int i=0; i<commitsModel.getObject().size(); i++) {
			RevCommit commit = commitsModel.getObject().get(i);
			if (commit.name().equals(commitHash)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Nullable
	private Mark getDiffMark(Mark mark) {
		if (mark.getCommitHash().equals(state.oldCommitHash)) {
			return mark.mapTo(getProject(), getComparisonBase());
		} else if (mark.getCommitHash().equals(state.newCommitHash) 
				|| mark.getCommitHash().equals(getComparisonBase().name())) {
			return mark;
		} else {
			return null;
		}
	}
	
	@Nullable
	private Mark getPermanentMark(Mark mark) {
		ObjectId oldCommitId = ObjectId.fromString(state.oldCommitHash);
		if (mark.getCommitHash().equals(getComparisonBase().name())) {
			return mark.mapTo(getProject(), oldCommitId);
		} else if (mark.getCommitHash().equals(state.oldCommitHash) 
					|| mark.getCommitHash().equals(state.newCommitHash)) {
			return mark;
		} else {
			return null;
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(head = new WebMarkupContainer("changesHead"));
		head.add(new WebSocketObserver() {

			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(PullRequest.getWebSocketObservable(getPullRequest().getId()));
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
			}
			
		});
		head.setOutputMarkupId(true);
		
		head.add(new AjaxLink<Void>("prevCommitLink") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);

				configure();
				if (!isEnabled()) {
					tag.put("disabled", "disabled");
				}
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(getCommitIndex(state.oldCommitHash) != -1 && getCommitIndex(state.newCommitHash) != -1);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = getCommitIndex(state.oldCommitHash);
				if (index != -1) {
					state.newCommitHash = commitsModel.getObject().get(index).name();
					index--;
					if (index == -1) {
						state.oldCommitHash = getPullRequest().getBaseCommitHash();
					} else {
						state.oldCommitHash = commitsModel.getObject().get(index).name();
					}
					newRevisionDiff(target);
					OneDev.getInstance(WebSocketManager.class).observe(PullRequestChangesPage.this);
				}
				target.add(head);
				pushState(target);
			}
			
		});
		head.add(new AjaxLink<Void>("nextCommitLink") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				int oldIndex = getCommitIndex(state.oldCommitHash);
				int newIndex = getCommitIndex(state.newCommitHash);
				if (!state.oldCommitHash.equals(getPullRequest().getBaseCommitHash()) && oldIndex == -1 
						|| newIndex == -1 || newIndex == commitsModel.getObject().size()-1) {
					setEnabled(false);
				} else {
					setEnabled(true);
				}
				
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				configure();
				if (!isEnabled()) {
					tag.put("disabled", "disabled");
				}
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = getCommitIndex(state.newCommitHash);
				if (index != -1 && index != commitsModel.getObject().size()-1) {
					CodeComment comment = getOpenComment();
					
					// we will not move old commit if an opened comment points to it
					if (comment == null || comment.getMark().getCommitHash().equals(state.newCommitHash)) {
						state.oldCommitHash = state.newCommitHash;
					}
					index++;
					state.newCommitHash = commitsModel.getObject().get(index).name();
					newRevisionDiff(target);
					OneDev.getInstance(WebSocketManager.class).observe(PullRequestChangesPage.this);
				} 
				target.add(head);
				pushState(target);
			}
			
		});
		
		DropdownLink selectedCommitsLink = new DropdownLink("comparingCommits", new AlignPlacement(50, 100, 50, 0)) {
			
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				AbstractPostAjaxBehavior callbackBehavior = new AbstractPostAjaxBehavior() {
					
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
					}

					@Override
					protected void respond(AjaxRequestTarget target) {
						IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
						state.oldCommitHash = params.getParameterValue("oldCommit").toString();
						state.newCommitHash = params.getParameterValue("newCommit").toString();
						target.add(head);
						newRevisionDiff(target);
						OneDev.getInstance(WebSocketManager.class).observe(PullRequestChangesPage.this);
						pushState(target);
						dropdown.close();
					}
					
				};
				Fragment fragment = new Fragment(id, "commitsFrag", PullRequestChangesPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						
						String callback = callbackBehavior.getCallbackFunction(explicit("oldCommit"), explicit("newCommit")).toString();
						String script;
						int oldIndex = getCommitIndex(state.oldCommitHash);
						int newIndex = getCommitIndex(state.newCommitHash);
						if ((state.oldCommitHash.equals(getPullRequest().getBaseCommitHash()) || oldIndex != -1) 
								&& newIndex != -1) {
							script = String.format("onedev.server.requestChanges.initCommitSelector(%s, '%s', %d, %d);", 
									callback, getPullRequest().getBaseCommitHash(), oldIndex+1, newIndex);
						} else {
							script = String.format("onedev.server.requestChanges.initCommitSelector(%s, '%s');", 
									callback, getPullRequest().getBaseCommitHash());
						}
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				fragment.add(callbackBehavior);
				fragment.add(new Link<Void>("allChanges") {

					@Override
					public void onClick() {
						PullRequestChangesPage.State state = new PullRequestChangesPage.State();
						
						PullRequest request = getPullRequest();
						state.oldCommitHash = request.getBaseCommitHash();
						state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
						setResponsePage(PullRequestChangesPage.class, 
								PullRequestChangesPage.paramsOf(request, state));
					}
					
				});
				
				fragment.add(new Link<Date>("changesSinceLastReview", new LoadableDetachableModel<Date>() {

					@Override
					protected Date load() {
						Date lastReviewDate = null;
						User user = getLoginUser();
						if (user != null) {
							PullRequest request = getPullRequest();
							for (PullRequestComment comment: request.getComments()) { 
								if (comment.getUser().equals(user) && 
										(lastReviewDate == null || lastReviewDate.before(comment.getDate()))) {
									lastReviewDate = comment.getDate();
								}
							}
							
							for (PullRequestChange change: request.getChanges()) {
								if (user.equals(change.getUser()) 
										&& (lastReviewDate == null || lastReviewDate.before(change.getDate()))
										&& (change.getData() instanceof PullRequestApproveData 
												|| change.getData() instanceof PullRequestReopenData
												|| change.getData() instanceof PullRequestRequestedForChangesData)) {
									lastReviewDate = change.getDate();
								}
							}
							
							for (CodeComment comment: request.getCodeComments()) {
								if (user.equals(comment.getUser()) && 
										(lastReviewDate == null || lastReviewDate.before(comment.getCreateDate()))) {
									lastReviewDate = comment.getCreateDate();
								}
								for (CodeCommentReply reply: comment.getReplies()) {
									if (user.equals(reply.getUser()) && 
											(lastReviewDate == null || lastReviewDate.before(reply.getDate()))) {
										lastReviewDate = reply.getDate();
									}
								}
							}
						}
						return lastReviewDate;
					}
					
				}) {

					@Override
					public void onClick() {
						PullRequestChangesPage.State state = new PullRequestChangesPage.State();
						
						PullRequest request = getPullRequest();
						state.oldCommitHash = request.getBaseCommitHash();
						for (PullRequestUpdate update: request.getSortedUpdates()) {
							if (update.getDate().before(getModelObject()))
								state.oldCommitHash = update.getHeadCommitHash();
						}
						
						state.newCommitHash = request.getLatestUpdate().getHeadCommitHash();
						setResponsePage(PullRequestChangesPage.class, 
								PullRequestChangesPage.paramsOf(request, state));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						Date lastReviewDate = getModelObject();
						setVisible(lastReviewDate != null 
								&& lastReviewDate.before(getPullRequest().getLatestUpdate().getDate()));
					}
					
				});
				fragment.add(new ListView<RevCommit>("commits", commitsModel) {

					@Override
					protected void populateItem(ListItem<RevCommit> item) {
						RevCommit commit = item.getModelObject();
						if (!getPullRequest().getPendingCommits().contains(commit)) {
							item.add(AttributeAppender.append("class", "rebased"));
							item.add(AttributeAppender.append("title", "This commit is rebased"));
						}
						item.add(AttributeAppender.append("data-hash", commit.name()));
						item.add(new Label("hash", GitUtils.abbreviateSHA(commit.name())));
						item.add(new Label("subject", commit.getShortMessage()));
					}
					
				});
				return fragment;
			}
		};
		selectedCommitsLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String oldName;
				if (state.oldCommitHash.equals(getPullRequest().getBaseCommitHash()))
					oldName = "base";
				else
					oldName = GitUtils.abbreviateSHA(state.oldCommitHash);

				String newName;
				if (state.newCommitHash.equals(getPullRequest().getLatestUpdate().getHeadCommitHash())) {
					newName = "head";
				} else {
					newName = GitUtils.abbreviateSHA(state.newCommitHash);
				}
				
				return oldName + " ... " + newName;
			}
			
		}));
		
		head.add(selectedCommitsLink);
		
		head.add(new AjaxLink<Void>("fullChanges") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				state.oldCommitHash = getPullRequest().getBaseCommitHash();
				state.newCommitHash = getPullRequest().getLatestUpdate().getHeadCommitHash();
				target.add(head);
				newRevisionDiff(target);
				OneDev.getInstance(WebSocketManager.class).observe(PullRequestChangesPage.this);
				pushState(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!state.oldCommitHash.equals(getPullRequest().getBaseCommitHash()) 
						|| !state.newCommitHash.equals(getPullRequest().getLatestUpdate().getHeadCommitHash()));
			}
			
		});
		
		newRevisionDiff(null);
	}

	@Override
	public void onDetach() {
		commitsModel.detach();
		comparisonBaseModel.detach();
		commentsModel.detach();
		openCommentModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, String oldCommit, String newCommit) {
		State state = new State();
		state.oldCommitHash = oldCommit;
		state.newCommitHash = newCommit;
		return paramsOf(request, state);
	}

	public static State getState(CodeComment comment) {
		PullRequestChangesPage.State state = new PullRequestChangesPage.State();
		state.commentId = comment.getId();
		state.mark = comment.getMark();
		state.pathFilter = comment.getCompareContext().getPathFilter();
		state.whitespaceOption = comment.getCompareContext().getWhitespaceOption();
		return state;
	}
	
	public static PageParameters paramsOf(PullRequest request, CodeComment comment) {
		return paramsOf(request, getState(comment));
	}
	
	public static PageParameters paramsOf(PullRequest request, State state) {
		PageParameters params = PullRequestDetailPage.paramsOf(request);
		fillParams(params, state);
		return params;
	}
	
	public static void fillParams(PageParameters params, State state) {
		if (state.oldCommitHash != null)
			params.add(PARAM_OLD_COMMIT, state.oldCommitHash);
		if (state.newCommitHash != null)
			params.add(PARAM_NEW_COMMIT, state.newCommitHash);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.add(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.add(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.add(PARAM_BLAME_FILE, state.blameFile);
		if (state.commentId != null)
			params.add(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.add(PARAM_MARK, state.mark.toString());
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		state = (State) data;
		newRevisionDiff(target);
		OneDev.getInstance(WebSocketManager.class).observe(this);
	}
	
	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), state);
		CharSequence url = RequestCycle.get().urlFor(PullRequestChangesPage.class, params);
		pushState(partialPageRequestHandler, url.toString(), state);
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
				pushState(RequestCycle.get().find(AjaxRequestTarget.class));
			}

		};
		
		Component revisionDiff = new RevisionDiffPanel("revisionDiff", projectModel,  
				requestModel, getComparisonBase().name(), state.newCommitHash, 
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
		response.render(JavaScriptHeaderItem.forReference(new PullRequestChangesResourceReference()));
	}

	@Override
	public Mark getMark() {
		if (state.mark != null)
			return getDiffMark(state.mark);
		else
			return null;
	}

	@Override
	public String getMarkUrl(Mark mark) {
		State markState = new State();
		markState.mark = getPermanentMark(mark);
		if (markState.mark != null) {
			markState.oldCommitHash = state.oldCommitHash;
			markState.newCommitHash = state.newCommitHash;
			markState.pathFilter = state.pathFilter;
			markState.whitespaceOption = state.whitespaceOption;
			return urlFor(PullRequestChangesPage.class, paramsOf(getPullRequest(), markState)).toString();
		} else {
			return null;
		}
	}

	@Override
	public CodeComment getOpenComment() {
		return openCommentModel.getObject();
	}

	private ObjectId getComparisonBase() {
		return comparisonBaseModel.getObject();
	}
	
	@Override
	public Collection<CodeComment> getComments() {
		return commentsModel.getObject();
	}
	
	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		state.commentId = comment.getId();
		state.mark = getPermanentMark(comment.getMark());
		OneDev.getInstance(WebSocketManager.class).observe(this);
		pushState(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		state.commentId = null;
		state.mark = null;
		OneDev.getInstance(WebSocketManager.class).observe(this);
		pushState(target);
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, Mark mark) {
		state.mark = getPermanentMark(mark);
		pushState(target);
	}
	
	@Override
	public void onUnmark(AjaxRequestTarget target) {
		state.mark = null;
		pushState(target);
	}
	
	public State getState() {
		return state;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, Mark mark) {
		state.commentId = null;
		state.mark = getPermanentMark(mark);
		pushState(target);
		OneDev.getInstance(WebSocketManager.class).observe(this);
	}

	private void saveCommentOrReply(CodeComment comment, @Nullable CodeCommentReply reply) {
		Mark prevMark = comment.getMark();
		CompareContext prevCompareContext = comment.getCompareContext();
		try {
			comment.setMark(Preconditions.checkNotNull(getPermanentMark(prevMark)));
			if (prevCompareContext.getCompareCommitHash().equals(getComparisonBase().name())) {
				CompareContext compareContext = new CompareContext();
				compareContext.setLeftSide(prevCompareContext.isLeftSide());
				compareContext.setPathFilter(prevCompareContext.getPathFilter());
				compareContext.setWhitespaceOption(prevCompareContext.getWhitespaceOption());
				compareContext.setCompareCommitHash(state.oldCommitHash);
				comment.setCompareContext(compareContext);
			}
			if (reply != null)
				OneDev.getInstance(CodeCommentReplyManager.class).save(reply);
			else
				OneDev.getInstance(CodeCommentManager.class).save(comment);
		} finally {
			comment.setMark(prevMark);
			comment.setCompareContext(prevCompareContext);
		}
	}
	
	@Override
	public void onSaveComment(CodeComment comment) {
		saveCommentOrReply(comment, null);
	}
	
	@Override
	public void onSaveCommentReply(CodeCommentReply reply) {
		saveCommentOrReply(reply.getComment(), reply);
	}
	
	@Override
	public PageParameters getParamsBeforeEdit() {
		return paramsOf(getPullRequest(), state);
	}

	@Override
	public PageParameters getParamsAfterEdit() {
		PageParameters params = getParamsBeforeEdit();
		params.remove(PARAM_NEW_COMMIT);
		if (getOpenComment() != null)
			params.set(PARAM_OLD_COMMIT, getOpenComment().getMark().getCommitHash());
		else
			params.remove(PARAM_OLD_COMMIT);
		return params;
	}
	
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String oldCommitHash;
		
		public String newCommitHash;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Long commentId;
		
		@Nullable
		public Mark mark;
		
	}

}

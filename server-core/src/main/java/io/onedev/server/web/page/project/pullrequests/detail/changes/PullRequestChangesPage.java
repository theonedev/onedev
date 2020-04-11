package io.onedev.server.web.page.project.pullrequests.detail.changes;

import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestUpdate;
import io.onedev.server.model.support.MarkPos;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.diff.revision.CommentSupport;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.util.EditParamsAware;
import io.onedev.server.web.util.Cursor;
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
	
	public PullRequestChangesPage(PageParameters params) {
		super(params);

		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = MarkPos.fromString(params.get(PARAM_MARK).toString());
		
		state.oldCommit = params.get(PARAM_OLD_COMMIT).toString();
		state.newCommit = params.get(PARAM_NEW_COMMIT).toString();
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
		     
		PullRequest request = getPullRequest();
		if (state.commentId != null) {
			CodeComment comment = OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
			CodeComment.ComparingInfo commentComparingInfo = comment.getComparingInfo();
			PullRequest.ComparingInfo requestComparingInfo = 
					getPullRequest().getRequestComparingInfo(commentComparingInfo);
			if (requestComparingInfo != null && state.oldCommit == null && state.newCommit == null) {
				if (comment.isContextChanged(request)) {
					state.oldCommit = comment.getMarkPos().getCommit();
					state.newCommit = request.getHeadCommitHash();
				} else {
					state.oldCommit = requestComparingInfo.getOldCommit();
					state.newCommit = requestComparingInfo.getNewCommit();
				}
			} 
		} 
		if (state.oldCommit == null) 
			state.oldCommit = request.getBaseCommitHash();
		if (state.newCommit == null)
			state.newCommit = request.getHeadCommitHash();
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
	
	private void onRegionChange() {
		OneDev.getInstance(WebSocketManager.class).observe(this);
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
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
				setEnabled(getCommitIndex(state.oldCommit) != -1 && getCommitIndex(state.newCommit) != -1);
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				int index = getCommitIndex(state.oldCommit);
				if (index != -1) {
					state.newCommit = commitsModel.getObject().get(index).name();
					index--;
					if (index == -1) {
						state.oldCommit = getPullRequest().getBaseCommitHash();
					} else {
						state.oldCommit = commitsModel.getObject().get(index).name();
					}
					newRevisionDiff(target);
					onRegionChange();
				}
				target.add(head);
				pushState(target);
			}
			
		});
		head.add(new AjaxLink<Void>("nextCommitLink") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				int oldIndex = getCommitIndex(state.oldCommit);
				int newIndex = getCommitIndex(state.newCommit);
				if (!state.oldCommit.equals(getPullRequest().getBaseCommitHash()) && oldIndex == -1 
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
				int index = getCommitIndex(state.newCommit);
				if (index != -1 && index != commitsModel.getObject().size()-1) {
					CodeComment comment = getOpenComment();
					
					// we will not move old commit if an opened comment points to it
					if (comment == null || comment.getMarkPos().getCommit().equals(state.newCommit)) {
						state.oldCommit = state.newCommit;
					}
					index++;
					state.newCommit = commitsModel.getObject().get(index).name();
					newRevisionDiff(target);
					onRegionChange();
				} 
				target.add(head);
				pushState(target);
			}
			
		});
		
		DropdownLink selectedCommitsLink = new DropdownLink("comparingCommits") {
			
			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				AbstractPostAjaxBehavior callbackBehavior = new AbstractPostAjaxBehavior() {
					
					@Override
					protected void respond(AjaxRequestTarget target) {
						IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
						state.oldCommit = params.getParameterValue("oldCommit").toString();
						state.newCommit = params.getParameterValue("newCommit").toString();
						target.add(head);
						newRevisionDiff(target);
						onRegionChange();
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
						int oldIndex = getCommitIndex(state.oldCommit);
						int newIndex = getCommitIndex(state.newCommit);
						if ((state.oldCommit.equals(getPullRequest().getBaseCommitHash()) || oldIndex != -1) 
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
				fragment.add(new ListView<RevCommit>("commits", commitsModel) {

					@Override
					protected void populateItem(ListItem<RevCommit> item) {
						RevCommit commit = item.getModelObject();
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
				if (state.oldCommit.equals(getPullRequest().getBaseCommitHash()))
					oldName = "base";
				else
					oldName = GitUtils.abbreviateSHA(state.oldCommit);

				String newName;
				if (state.newCommit.equals(getPullRequest().getHeadCommitHash())) {
					newName = "head";
				} else {
					newName = GitUtils.abbreviateSHA(state.newCommit);
				}
				
				return oldName + " ... " + newName;
			}
			
		}));
		
		head.add(selectedCommitsLink);
		
		head.add(new AjaxLink<Void>("fullChanges") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				state.oldCommit = getPullRequest().getBaseCommitHash();
				state.newCommit = getPullRequest().getHeadCommitHash();
				target.add(head);
				newRevisionDiff(target);
				onRegionChange();
				pushState(target);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!state.oldCommit.equals(getPullRequest().getBaseCommitHash()) 
						|| !state.newCommit.equals(getPullRequest().getHeadCommitHash()));
			}
			
		});
		
		newRevisionDiff(null);
	}

	@Override
	public void onDetach() {
		commitsModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable Cursor cursor, String oldCommit, String newCommit) {
		State state = new State();
		state.oldCommit = oldCommit;
		state.newCommit = newCommit;
		return paramsOf(request, cursor, state);
	}

	public static State getState(CodeComment comment) {
		PullRequestChangesPage.State state = new PullRequestChangesPage.State();
		state.commentId = comment.getId();
		state.mark = comment.getMarkPos();
		state.pathFilter = comment.getCompareContext().getPathFilter();
		state.whitespaceOption = comment.getCompareContext().getWhitespaceOption();
		return state;
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable Cursor cursor, CodeComment comment) {
		return paramsOf(request, cursor, getState(comment));
	}
	
	public static PageParameters paramsOf(PullRequest request, @Nullable Cursor cursor, State state) {
		PageParameters params = PullRequestDetailPage.paramsOf(request, cursor);
		fillParams(params, state);
		return params;
	}
	
	public static void fillParams(PageParameters params, State state) {
		if (state.oldCommit != null)
			params.add(PARAM_OLD_COMMIT, state.oldCommit);
		if (state.newCommit != null)
			params.add(PARAM_NEW_COMMIT, state.newCommit);
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
		onRegionChange();
	}
	
	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), getCursor(), state);
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
				requestModel, state.oldCommit, state.newCommit, pathFilterModel, 
				whitespaceOptionModel, blameModel, this);
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
	public MarkPos getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(MarkPos mark) {
		State markState = new State();
		markState.mark = mark;
		markState.oldCommit = state.oldCommit;
		markState.newCommit = state.newCommit;
		markState.pathFilter = state.pathFilter;
		markState.whitespaceOption = state.whitespaceOption;
		return urlFor(PullRequestChangesPage.class, paramsOf(getPullRequest(), getCursor(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		commentState.mark = comment.getMarkPos();
		commentState.commentId = comment.getId();
		commentState.oldCommit = state.oldCommit;
		commentState.newCommit = state.newCommit;
		commentState.pathFilter = state.pathFilter;
		commentState.whitespaceOption = state.whitespaceOption;
		return urlFor(PullRequestChangesPage.class, paramsOf(getPullRequest(), getCursor(), commentState)).toString();
	}
	
	@Override
	public CodeComment getOpenComment() {
		if (state.commentId != null)
			return OneDev.getInstance(CodeCommentManager.class).load(state.commentId);
		else
			return null;
	}

	@Override
	public void onCommentOpened(AjaxRequestTarget target, CodeComment comment) {
		if (comment != null) {
			state.commentId = comment.getId();
			state.mark = comment.getMarkPos();
		} else {
			state.commentId = null;
			state.mark = null;
		}
		onRegionChange();
		pushState(target);
	}

	@Override
	public void onMark(AjaxRequestTarget target, MarkPos mark) {
		state.mark = mark;
		pushState(target);
	}
	
	public State getState() {
		return state;
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, MarkPos mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
		onRegionChange();
	}

	@Override
	public PageParameters getParamsBeforeEdit() {
		return paramsOf(getPullRequest(), getCursor(), state);
	}

	@Override
	public PageParameters getParamsAfterEdit() {
		PageParameters params = getParamsBeforeEdit();
		params.remove(PARAM_NEW_COMMIT);
		if (getOpenComment() != null)
			params.set(PARAM_OLD_COMMIT, getOpenComment().getMarkPos().getCommit());
		else
			params.remove(PARAM_OLD_COMMIT);
		return params;
	}
	
	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public String oldCommit;
		
		public String newCommit;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Long commentId;
		
		@Nullable
		public MarkPos mark;
		
	}

}

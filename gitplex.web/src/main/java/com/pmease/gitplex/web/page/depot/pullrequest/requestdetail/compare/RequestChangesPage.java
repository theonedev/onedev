package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
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
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.wicket.component.DropdownLink;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.MarkSupport;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestlist.RequestListPage;

@SuppressWarnings("serial")
public class RequestChangesPage extends RequestDetailPage implements MarkSupport {

	private static final String PARAM_OLD_COMMIT = "old-commit";
	
	private static final String PARAM_NEW_COMMIT = "new-commit";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private State state = new State();
	
	private WebMarkupContainer head;
	
	private final IModel<List<BriefCommit>> commitsModel = new LoadableDetachableModel<List<BriefCommit>>() {

		@Override
		protected List<BriefCommit> load() {
			List<BriefCommit> commits = new ArrayList<>();
			for (PullRequestUpdate update: getPullRequest().getSortedUpdates()) {
				commits.addAll(update.getCommits());
			}
			return commits;
		}
		
	};
	
	public RequestChangesPage(PageParameters params) {
		super(params);

		state.oldCommit = params.get(PARAM_OLD_COMMIT).toString();
		state.newCommit = params.get(PARAM_NEW_COMMIT).toString();
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = DiffMark.of(params.get(PARAM_MARK).toString());
	}
	
	private int getCommitIndex(String commitHash) {
		int index = -1;
		for (int i=0; i<commitsModel.getObject().size(); i++) {
			BriefCommit commit = commitsModel.getObject().get(i);
			if (commit.getHash().equals(state.oldCommit)) {
				index = i;
				break;
			}
		}
		return index;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(head = new WebMarkupContainer("changesHead"));
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
					state.newCommit = commitsModel.getObject().get(index).getHash();
					index--;
					if (index == -1) {
						state.oldCommit = getPullRequest().getBaseCommitHash();
					} else {
						state.oldCommit = commitsModel.getObject().get(index).getHash();
					}
					newRevisionDiff(target);
				}
				target.add(head);
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
					state.oldCommit = commitsModel.getObject().get(index).getHash();
					index++;
					state.newCommit = commitsModel.getObject().get(index).getHash();
					newRevisionDiff(target);
				} 
				target.add(head);
			}
			
		});
		
		DropdownLink selectedCommitsLink = new DropdownLink("comparingCommits") {
			
			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "commitsFrag", RequestChangesPage.this) {

					@Override
					public void renderHead(IHeaderResponse response) {
						super.renderHead(response);
						String script;
						int oldIndex = getCommitIndex(state.oldCommit);
						int newIndex = getCommitIndex(state.newCommit);
						if ((state.oldCommit.equals(getPullRequest().getBaseCommitHash()) || oldIndex != -1) 
								&& newIndex != -1) {
							script = String.format("gitplex.requestChanges.initCommitSelector(%d, %d);", oldIndex+1, newIndex+1);
						} else {
							script = "gitplex.requestChanges.initCommitSelector();";
						}
						response.render(OnDomReadyHeaderItem.forScript(script));
					}
					
				};
				fragment.add(new ListView<BriefCommit>("commits", commitsModel) {

					@Override
					protected void populateItem(ListItem<BriefCommit> item) {
						item.add(new Label("hash", GitUtils.abbreviateSHA(item.getModelObject().getHash())));
						item.add(new Label("subject", item.getModelObject().getSubject()));
					}
					
				});
				return fragment;
			}
		};
		selectedCommitsLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.abbreviateSHA(state.oldCommit) + "..." + GitUtils.abbreviateSHA(state.newCommit);
			}
			
		}).setEscapeModelStrings(false));
		
		head.add(selectedCommitsLink);
		
		newRevisionDiff(null);
	}

	@Override
	public void onDetach() {
		commitsModel.detach();
		super.onDetach();
	}
	
	public static PageParameters paramsOf(PullRequest request, String oldCommit, String newCommit) {
		State state = new State();
		state.oldCommit = oldCommit;
		state.newCommit = newCommit;
		return paramsOf(request, state);
	}
	
	public static PageParameters paramsOf(PullRequest request, State state) {
		PageParameters params = RequestDetailPage.paramsOf(request);

		if (state.oldCommit != null)
			params.set(PARAM_OLD_COMMIT, state.oldCommit);
		if (state.newCommit != null)
			params.set(PARAM_NEW_COMMIT, state.newCommit);
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
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);

		state = (State) data;
		
		newRevisionDiff(target);
	}
	
	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RequestListPage.class, paramsOf(depot));
	}

	private void pushState(IPartialPageRequestHandler partialPageRequestHandler) {
		PageParameters params = paramsOf(getPullRequest(), state);
		CharSequence url = RequestCycle.get().urlFor(RequestChangesPage.class, params);
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
		
		Component revisionDiff = new RevisionDiffPanel("revisionDiff", depotModel,  
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
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(RequestChangesPage.class, "request-changes.js")));
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RequestChangesPage.class, "request-changes.css")));
	}

	@Override
	public DiffMark getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(DiffMark mark) {
		State markState = new State();
		markState.mark = mark;
		markState.oldCommit = state.oldCommit;
		markState.newCommit = state.newCommit;
		markState.pathFilter = state.pathFilter;
		markState.whitespaceOption = state.whitespaceOption;
		return urlFor(RequestChangesPage.class, paramsOf(getPullRequest(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		commentState.mark = new DiffMark(comment);
		commentState.commentId = comment.getId();
		commentState.oldCommit = state.oldCommit;
		commentState.newCommit = state.newCommit;
		commentState.pathFilter = state.pathFilter;
		commentState.whitespaceOption = state.whitespaceOption;
		return urlFor(RequestChangesPage.class, paramsOf(getPullRequest(), commentState)).toString();
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
			state.mark = new DiffMark(comment);
		} else {
			state.commentId = null;
		}
		pushState(target);
	}

	@Override
	public void onMark(AjaxRequestTarget target, DiffMark mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onAddComment(AjaxRequestTarget target, DiffMark mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
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
		public DiffMark mark;
		
	}

}

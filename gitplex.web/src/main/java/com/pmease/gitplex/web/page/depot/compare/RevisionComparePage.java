package com.pmease.gitplex.web.page.depot.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.ObjectId;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.lang.diff.WhitespaceOption;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.commons.wicket.component.backtotop.BackToTop;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.CodeComment;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.component.DepotAndBranch;
import com.pmease.gitplex.core.entity.component.DepotAndRevision;
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.MarkSupport;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class RevisionComparePage extends DepotPage implements MarkSupport {

	private static final String PARAM_LEFT = "left";
	
	private static final String PARAM_RIGHT = "right";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private AjaxActionTab commitsTab;
	
	private AjaxActionTab filesTab;
	
	private State state = new State();
	
	private ObjectId resolvedRightSideRevision;
	
	public static PageParameters paramsOf(Depot depot, State state) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_LEFT, state.leftSide.toString());
		params.set(PARAM_RIGHT, state.rightSide.toString());
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		return params;
	}

	public RevisionComparePage(final PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));

		String str = params.get(PARAM_RIGHT).toString();
		if (str != null) {
			state.rightSide = new DepotAndRevision(str);
		} else {
			state.rightSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		resolvedRightSideRevision = state.rightSide.getCommit().copy();
		
		str = params.get(PARAM_LEFT).toString();
		if (str != null) {
			state.leftSide = new DepotAndRevision(str);
		} else {
			state.leftSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = DiffMark.of(params.get(PARAM_MARK).toString());
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				DepotAndBranch left = new DepotAndBranch(state.leftSide.toString());
				DepotAndBranch right = new DepotAndBranch(state.rightSide.toString());
				return GitPlex.getInstance(PullRequestManager.class).findOpen(left, right);
			}
			
		};

		mergeBaseModel = new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				Depot leftDepot = state.leftSide.getDepot();
				Depot rightDepot = state.rightSide.getDepot();
				if (!leftDepot.equals(rightDepot)) {
					Git tempGit = new Git(FileUtils.createTempDir());
					try {
						tempGit.clone(leftDepot.git(), false, true, true, state.leftSide.getRevision());
						tempGit.reset(null, null);
						tempGit.fetch(rightDepot.git(), state.rightSide.getRevision());
						return tempGit.calcMergeBase(state.leftSide.getCommit().name(), state.rightSide.getCommit().name());
					} finally {
						FileUtils.deleteDir(tempGit.depotDir());
					}
				} else {
					return leftDepot.getMergeBase(state.leftSide.getRevision(), state.rightSide.getRevision()).name();
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Depot rightDepot = state.rightSide.getDepot();
				
				// for right side, we use resolved commit name instead of revision 
				// to make sure that revision is resolved consistently
				return rightDepot.git().log(mergeBaseModel.getObject(), 
						state.rightSide.getCommit().name(), null, 0, 0, false);
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("leftSide", state.leftSide.getDepotId(), state.leftSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				State newState = new State();
				newState.leftSide = new DepotAndRevision(depot, revision);
				newState.rightSide = state.rightSide;
				newState.pathFilter = state.pathFilter;
				newState.whitespaceOption = state.whitespaceOption;

				PageParameters params = paramsOf(depot, newState);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		add(new AffinalRevisionPicker("rightSide", 
				state.rightSide.getDepotId(), state.rightSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				State newState = new State();
				newState.leftSide = state.leftSide;
				newState.rightSide = new DepotAndRevision(depot, revision);
				newState.pathFilter = state.pathFilter;
				newState.whitespaceOption = state.whitespaceOption;
				
				PageParameters params = paramsOf(getDepot(), newState);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				State newState = new State();
				newState.leftSide = state.rightSide;
				newState.rightSide = state.leftSide;
				newState.pathFilter = state.pathFilter;
				newState.whitespaceOption = state.whitespaceOption;
				setResponsePage(RevisionComparePage.class,paramsOf(getDepot(), newState));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!mergeBaseModel.getObject().equals(state.leftSide.getCommit().name()));
			}

		}.add(new TooltipBehavior(Model.of("Left side is ahead of right side, swap to see changes"))));
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				DepotAndBranch left = new DepotAndBranch(state.leftSide.toString());
				DepotAndBranch right = new DepotAndBranch(state.rightSide.toString());
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(left.getDepot(), left, right));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (state.leftSide.getBranch()!=null && state.rightSide.getBranch()!=null) {
					PullRequest request = requestModel.getObject();
					setVisible(request == null && !mergeBaseModel.getObject().equals(state.rightSide.getCommit().name()));
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new WebMarkupContainer("openedRequest") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = requestModel.getObject();
				setVisible(request != null);
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new Label("no", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return requestModel.getObject().getId().toString();
					}
				}));
				add(new Link<Void>("link") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return requestModel.getObject().getTitle();
							}
						}));
					}

					@Override
					public void onClick() {
						PageParameters params = RequestDetailPage.paramsOf(requestModel.getObject());
						setResponsePage(RequestOverviewPage.class, params);
					}
					
				});
				
			}
			
		});
		add(new WebMarkupContainer("noChanges") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!hasChanges());
			}

		});
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(commitsTab = new AjaxActionTab(Model.of("Commits")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				state.pathFilter = null;
				newTabPanel(target);
				pushState(target);
			}
			
		});

		tabs.add(filesTab = new AjaxActionTab(Model.of("Changed Files")) {
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				state.pathFilter = "";
				newTabPanel(target);
				pushState(target);
			}
			
		});

		add(new Tabbable("tabs", tabs) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasChanges());
			}

		});

		newTabPanel(null);
		
		add(new BackToTop("backToTop"));
	}
	
	private boolean hasChanges() {
		return !mergeBaseModel.getObject().equals(state.rightSide.getCommit().name());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionComparePage.class, "revision-compare.css")));
	}

	private void newTabPanel(@Nullable AjaxRequestTarget target) {
		WebMarkupContainer tabPanel;
		if (state.pathFilter != null) {
			IModel<Depot> depotModel = new LoadableDetachableModel<Depot>() {

				@Override
				protected Depot load() {
					Depot depot = state.rightSide.getDepot();
					depot.cacheObjectId(state.rightSide.getRevision(), resolvedRightSideRevision);
					return depot;
				}
				
			};
			tabPanel = new RevisionDiffPanel(TAB_PANEL_ID, depotModel, 
					new Model<PullRequest>(null), mergeBaseModel.getObject(), 
					state.rightSide.getRevision(), state.pathFilter, 
					state.whitespaceOption, this) {

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(hasChanges());
				}

				@Override
				protected void onPathFilterChange(AjaxRequestTarget target, String pathFilter) {
					state.pathFilter = pathFilter;
					pushState(target);
				}

				@Override
				protected void onWhitespaceOptionChange(AjaxRequestTarget target,
						WhitespaceOption whitespaceOption) {
					state.whitespaceOption = whitespaceOption;
					pushState(target);
				}
				
			};
			commitsTab.setSelected(false);
			filesTab.setSelected(true);
		} else {
			tabPanel = new CommitListPanel(TAB_PANEL_ID, depotModel, commitsModel){

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(hasChanges());
				}
				
			};
			commitsTab.setSelected(true);
			filesTab.setSelected(false);
		}
		tabPanel.setOutputMarkupId(true);
		if (target != null) {
			replace(tabPanel);
			target.add(tabPanel);
		} else {
			add(tabPanel);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		PageParameters params = paramsOf(getDepot(), state);
		CharSequence url = RequestCycle.get().urlFor(RevisionComparePage.class, params);
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (State) data;
		newTabPanel(target);
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		mergeBaseModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Depot depot) {
		setResponsePage(RevisionComparePage.class, paramsOf(depot));
	}

	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;

		public DepotAndRevision leftSide;
		
		public DepotAndRevision rightSide;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;

		@Nullable
		public String pathFilter;
		
		@Nullable
		public Long commentId;

		@Nullable
		public DiffMark mark;
		
		public State() {
		}
		
		public State(State state) {
			leftSide = state.leftSide;
			rightSide = state.rightSide;
			whitespaceOption = state.whitespaceOption;
			pathFilter = state.pathFilter;
			commentId = state.commentId;
			mark = state.mark;
		}
		
	}

	@Override
	public DiffMark getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(DiffMark mark) {
		State markState = new State();
		markState.leftSide = new DepotAndRevision(state.rightSide.getDepot(), mergeBaseModel.getObject());
		markState.rightSide = new DepotAndRevision(state.rightSide.getDepot(), resolvedRightSideRevision.name());
		markState.mark = mark;
		markState.pathFilter = state.pathFilter;
		markState.whitespaceOption = state.whitespaceOption;
		return urlFor(RevisionComparePage.class, paramsOf(markState.rightSide.getDepot(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		commentState.leftSide = new DepotAndRevision(state.rightSide.getDepot(), mergeBaseModel.getObject());
		commentState.rightSide = new DepotAndRevision(state.rightSide.getDepot(), resolvedRightSideRevision.name());
		commentState.mark = new DiffMark(comment, mergeBaseModel.getObject(), resolvedRightSideRevision.name());
		commentState.commentId = comment.getId();
		commentState.pathFilter = state.pathFilter;
		commentState.whitespaceOption = state.whitespaceOption;
		return urlFor(RevisionComparePage.class, paramsOf(commentState.rightSide.getDepot(), commentState)).toString();
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
		state.mark = new DiffMark(comment, mergeBaseModel.getObject(), resolvedRightSideRevision.name());
		state.commentId = comment.getId();
		pushState(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		state.commentId = null;
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
	
}

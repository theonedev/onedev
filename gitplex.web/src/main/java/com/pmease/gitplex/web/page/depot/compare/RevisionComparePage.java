package com.pmease.gitplex.web.page.depot.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.collect.Lists;
import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.command.LogCommand;
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
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class RevisionComparePage extends DepotPage {

	private static final String PARAM_LEFT = "left";
	
	private static final String PARAM_RIGHT = "right";
	
	private static final String PARAM_COMPARE_WITH_MERGE_BASE = "compare-with-merge-base";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private AjaxActionTab commitsTab;
	
	private AjaxActionTab filesTab;
	
	private HistoryState state = new HistoryState();
	
	public static PageParameters paramsOf(Depot depot, HistoryState state) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_LEFT, state.leftSide.toString());
		params.set(PARAM_RIGHT, state.rightSide.toString());
		params.set(PARAM_COMPARE_WITH_MERGE_BASE, state.compareWithMergeBase);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.commentId != null)
			params.set(PARAM_COMMENT, state.commentId);
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
		
		str = params.get(PARAM_LEFT).toString();
		if (str != null) {
			state.leftSide = new DepotAndRevision(str);
		} else {
			state.leftSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		
		state.compareWithMergeBase = params.get(PARAM_COMPARE_WITH_MERGE_BASE).toBoolean(false);
		
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		
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
					Git sandbox = new Git(FileUtils.createTempDir());
					try {
						sandbox.clone(leftDepot.git(), false, true, true, state.leftSide.getRevision());
						sandbox.reset(null, null);
						sandbox.fetch(rightDepot.git(), state.rightSide.getRevision());
						return sandbox.calcMergeBase(state.leftSide.getCommit().name(), state.rightSide.getCommit().name());
					} finally {
						FileUtils.deleteDir(sandbox.depotDir());
					}
				} else {
					return leftDepot.getMergeBase(state.leftSide.getRevision(), state.rightSide.getRevision()).name();
				}
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				Depot sourceDepot = state.leftSide.getDepot();
				LogCommand log = new LogCommand(sourceDepot.git().depotDir());
				List<Commit> commits;
				if (!state.compareWithMergeBase) {
					String revisions = state.leftSide.getRevision() + "..." + state.rightSide.getRevision();
					commits = log.revisions(Lists.newArrayList(revisions)).call();
					commits.add(sourceDepot.git().showRevision(mergeBaseModel.getObject()));
				} else {
					commits = sourceDepot.git().log(state.leftSide.getRevision(), state.rightSide.getRevision(), null, 0, 0, false);
				}
				return commits;
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
				HistoryState state = new HistoryState(RevisionComparePage.this.state);
				state.leftSide = new DepotAndRevision(depot, revision);

				PageParameters params = paramsOf(getDepot(), state);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		add(new CheckBox("compareWithMergeBase", Model.of(state.compareWithMergeBase)).add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				HistoryState state = new HistoryState(RevisionComparePage.this.state);
				state.compareWithMergeBase = !state.compareWithMergeBase;

				PageParameters params = RevisionComparePage.paramsOf(depotModel.getObject(), state);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		}));
		String tooltip = "Check this to compare \"right side\" with common ancestor of left and right";
		add(new WebMarkupContainer("tooltip").add(new TooltipBehavior(Model.of(tooltip))));

		add(new AffinalRevisionPicker("rightSide", state.rightSide.getDepotId(), state.rightSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				HistoryState state = new HistoryState(RevisionComparePage.this.state);
				state.rightSide = new DepotAndRevision(depot, revision);
				
				PageParameters params = paramsOf(getDepot(), state);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		add(new Link<Void>("swap") {

			@Override
			public void onClick() {
				HistoryState state = new HistoryState(RevisionComparePage.this.state);
				state.leftSide = RevisionComparePage.this.state.rightSide;
				state.rightSide = RevisionComparePage.this.state.leftSide;
				setResponsePage(RevisionComparePage.class,paramsOf(getDepot(), state));
			}

		});
		
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
				
				if (state.leftSide.getBranch()!=null 
						&& state.rightSide.getBranch()!=null 
						&& state.compareWithMergeBase) {
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
				
				if (state.compareWithMergeBase) {
					PullRequest request = requestModel.getObject();
					setVisible(request != null);
				} else {
					setVisible(false);
				}
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
		add(new WebMarkupContainer("leftAhead") {
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new Link<Void>("compareWithMergeBase") {

					@Override
					public void onClick() {
						HistoryState state = new HistoryState(RevisionComparePage.this.state);
						state.compareWithMergeBase = true;
						PageParameters params = paramsOf(getDepot(), state);
						setResponsePage(RevisionComparePage.class, params);
					}
					
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!state.compareWithMergeBase 
						&& !mergeBaseModel.getObject().equals(state.leftSide.getCommit().name()));
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
		if (state.compareWithMergeBase) {
			return !mergeBaseModel.getObject().equals(state.rightSide.getCommit().name());
		} else {
			return !state.leftSide.getCommit().name().equals(state.rightSide.getCommit().name());
		}
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
			tabPanel = new RevisionDiffPanel(TAB_PANEL_ID, depotModel, 
					new Model<PullRequest>(null),  
					state.compareWithMergeBase?mergeBaseModel.getObject():state.leftSide.getRevision(), 
							state.rightSide.getRevision(), state.pathFilter, 
							state.whitespaceOption, state.commentId) {

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
				
				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(hasChanges());
				}

				@Override
				protected void onOpenComment(AjaxRequestTarget target, CodeComment comment) {
					state.commentId = CodeComment.idOf(comment);
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
		
		state = (HistoryState) data;
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

	public static class HistoryState implements Serializable {

		private static final long serialVersionUID = 1L;

		public HistoryState() {
		}
		
		public HistoryState(HistoryState copy) {
			leftSide = copy.leftSide;
			rightSide = copy.rightSide;
			compareWithMergeBase = copy.compareWithMergeBase;
			whitespaceOption = copy.whitespaceOption;
			pathFilter = copy.pathFilter;
			commentId = copy.commentId;
		}
		
		public DepotAndRevision leftSide;
		
		public DepotAndRevision rightSide;
		
		public boolean compareWithMergeBase = true;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;

		@Nullable
		public String pathFilter;
		
		@Nullable
		public Long commentId;
		
	}
	
}

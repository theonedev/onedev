package com.pmease.gitplex.web.page.depot.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.lib.ObjectId;

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
import com.pmease.gitplex.core.manager.CodeCommentManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.web.component.commitlist.CommitListPanel;
import com.pmease.gitplex.web.component.diff.revision.DiffMark;
import com.pmease.gitplex.web.component.diff.revision.MarkSupport;
import com.pmease.gitplex.web.component.diff.revision.RevisionDiffPanel;
import com.pmease.gitplex.web.component.revisionpicker.AffinalRevisionPicker;
import com.pmease.gitplex.web.page.depot.DepotPage;
import com.pmease.gitplex.web.page.depot.NoCommitsPage;
import com.pmease.gitplex.web.page.depot.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.newrequest.NewRequestPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.RequestDetailPage;
import com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class RevisionComparePage extends DepotPage implements MarkSupport {

	public enum TabPanel {
		COMMITS, 
		CHANGES;

		public static TabPanel of(@Nullable String name) {
			if (name != null) {
				return valueOf(name.toUpperCase());
			} else {
				return COMMITS;
			}
		}
		
	};
	
	private static final String PARAM_LEFT = "left";
	
	private static final String PARAM_RIGHT = "right";
	
	private static final String PARAM_COMPARE_WITH_MERGE_BASE = "compare-with-merge-base";
	
	private static final String PARAM_WHITESPACE_OPTION = "whitespace-option";
	
	private static final String PARAM_COMMENT = "comment";
	
	private static final String PARAM_MARK = "mark";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_TAB = "tab-panel";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private IModel<String> mergeBaseModel;

	private State state = new State();
	
	private ObjectId leftCommitId;
	
	private ObjectId rightCommitId;
	
	private Tabbable tabbable;
	
	public static PageParameters paramsOf(Depot depot, State state) {
		PageParameters params = paramsOf(depot);
		params.set(PARAM_LEFT, state.leftSide.toString());
		params.set(PARAM_RIGHT, state.rightSide.toString());
		params.set(PARAM_COMPARE_WITH_MERGE_BASE, state.compareWithMergeBase);
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
		if (state.tabPanel != null)
			params.set(PARAM_TAB, state.tabPanel.name());
		return params;
	}

	public RevisionComparePage(final PageParameters params) {
		super(params);
		
		if (!getDepot().git().hasRefs()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getDepot()));

		String str = params.get(PARAM_LEFT).toString();
		if (str != null) {
			state.leftSide = new DepotAndRevision(str);
		} else {
			state.leftSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		leftCommitId = state.leftSide.getCommit().copy();
		
		str = params.get(PARAM_RIGHT).toString();
		if (str != null) {
			state.rightSide = new DepotAndRevision(str);
		} else {
			state.rightSide = new DepotAndRevision(getDepot(), getDepot().getDefaultBranch());
		}
		rightCommitId = state.rightSide.getCommit().copy();
		
		state.compareWithMergeBase = params.get(PARAM_COMPARE_WITH_MERGE_BASE).toBoolean(true);
		
		/*
		 * When compare across different repositories, left revision and right revision might not 
		 * exist in same repository and this cause many difficulties such as calculating changes, 
		 * recording comment revisions, or get permanent mark urls. So we add below constraint as
		 * merge base commit and right side revision are guaranteed to be both in right side 
		 * repository  
		 */
		if (!state.compareWithMergeBase && !state.leftSide.getDepot().equals(state.rightSide.getDepot())) {
			throw new IllegalArgumentException("Can only compare with merge base when compare across repositories");
		}
		
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.of(params.get(PARAM_WHITESPACE_OPTION).toString());
		
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = DiffMark.of(params.get(PARAM_MARK).toString());
		
		state.tabPanel = TabPanel.of(params.get(PARAM_TAB).toString());
		
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
						return tempGit.calcMergeBase(leftCommitId.name(), rightCommitId.name());
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
				
				/*
				 * have to pass commit id here to log command even if we cached the object id as log command
				 * calls external git command and object id cache will not take effect
				 */
				if (rightDepot.equals(state.leftSide.getDepot())) {
					if (!state.compareWithMergeBase) {
						String revisions = leftCommitId.name() + "..." + rightCommitId.name();
						LogCommand log = new LogCommand(rightDepot.git().depotDir());
						List<Commit> commits = log.revisions(Lists.newArrayList(revisions)).call();
						// add the merge base commit to make the revision graph understandable 
						if (!mergeBaseModel.getObject().equals(leftCommitId.name()))
							commits.add(rightDepot.git().showRevision(mergeBaseModel.getObject()));
						return commits;
					} else {
						return rightDepot.git().log(mergeBaseModel.getObject(), rightCommitId.name(), null, 0, 0, false);
					}
				} else {
					return rightDepot.git().log(mergeBaseModel.getObject(), 
							rightCommitId.name(), null, 0, 0, false);
				}
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("leftRevSelector", state.leftSide.getDepotId(), state.leftSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				State newState = new State();
				newState.leftSide = new DepotAndRevision(depot, revision);
				newState.rightSide = state.rightSide;
				newState.pathFilter = state.pathFilter;
				newState.tabPanel = state.tabPanel;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = state.compareWithMergeBase;
				newState.mark = state.mark;
				newState.commentId = state.commentId;
				newState.tabPanel = state.tabPanel;

				PageParameters params = paramsOf(depot, newState);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		
		CheckBox checkBox = new CheckBox("compareWithMergeBase", Model.of(state.compareWithMergeBase)) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!mergeBaseModel.getObject().equals(leftCommitId.name()));
			}
			
		};
		checkBox.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				State newState = new State();
				newState.leftSide = state.leftSide;
				newState.rightSide = state.rightSide;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = !state.compareWithMergeBase;
				newState.commentId = state.commentId;
				newState.mark = state.mark;
				newState.tabPanel = state.tabPanel;
				
				PageParameters params = RevisionComparePage.paramsOf(depotModel.getObject(), newState);
				setResponsePage(RevisionComparePage.class, params);
			}
			
		});
		add(checkBox);

		String tooltip;
		if (!state.leftSide.getDepot().equals(state.rightSide.getDepot())) {
			checkBox.add(AttributeAppender.append("disabled", "disabled"));
			tooltip = "Can only compare with common ancestor when compare across different repositories";
		} else {
			tooltip = "Check this to compare \"right side\" with common ancestor of left and right";
		}
		
		add(new WebMarkupContainer("mergeBaseTooltip").add(new TooltipBehavior(Model.of(tooltip))));

		PageParameters params = CommitDetailPage.paramsOf(state.leftSide.getDepot(), state.leftSide.getCommit().name());
		Link<Void> leftCommitLink = new BookmarkablePageLink<Void>("leftCommitLink", CommitDetailPage.class, params);
		leftCommitLink.add(new Label("message", state.leftSide.getCommit().getShortMessage()));
		add(leftCommitLink);
		
		params = CommitDetailPage.paramsOf(state.rightSide.getDepot(), state.rightSide.getCommit().name());
		Link<Void> rightCommitLink = new BookmarkablePageLink<Void>("rightCommitLink", CommitDetailPage.class, params);
		rightCommitLink.add(new Label("message", state.rightSide.getCommit().getShortMessage()));
		add(rightCommitLink);
		
		add(new AffinalRevisionPicker("rightRevSelector", 
				state.rightSide.getDepotId(), state.rightSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Depot depot, String revision) {
				State newState = new State();
				newState.leftSide = state.leftSide;
				newState.rightSide = new DepotAndRevision(depot, revision);
				newState.pathFilter = state.pathFilter;
				newState.tabPanel = state.tabPanel;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = state.compareWithMergeBase;
				newState.commentId = state.commentId;
				newState.mark = state.mark;
				newState.tabPanel = state.tabPanel;
				
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
				newState.tabPanel = state.tabPanel;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = state.compareWithMergeBase;
				newState.mark = state.mark;
				newState.commentId = state.commentId;
				
				setResponsePage(RevisionComparePage.class,paramsOf(getDepot(), newState));
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
				
				if (state.leftSide.getBranch()!=null && state.rightSide.getBranch()!=null) {
					PullRequest request = requestModel.getObject();
					setVisible(request == null && !mergeBaseModel.getObject().equals(rightCommitId.name()));
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new WebMarkupContainer("openedRequest") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(requestModel.getObject() != null);
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
		
		List<Tab> tabs = new ArrayList<>();
		
		tabs.add(new AjaxActionTab(Model.of("Commits")) {
			
			@Override
			public boolean isSelected() {
				return state.tabPanel == null || state.tabPanel == TabPanel.COMMITS;
			}

			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				state.tabPanel = TabPanel.COMMITS;
				newTabPanel(target);
				pushState(target);
			}
			
		});

		tabs.add(new AjaxActionTab(Model.of("Changes")) {
			
			@Override
			public boolean isSelected() {
				return state.tabPanel == TabPanel.CHANGES;
			}
			
			@Override
			protected void onSelect(AjaxRequestTarget target, Component tabLink) {
				state.tabPanel = TabPanel.CHANGES;
				newTabPanel(target);
				pushState(target);
			}
			
		});

		add(tabbable = new Tabbable("tabs", tabs));

		newTabPanel(null);
		
		add(new BackToTop("backToTop"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(RevisionComparePage.class, "revision-compare.css")));
	}

	private void newTabPanel(@Nullable AjaxRequestTarget target) {
		IModel<Depot> depotModel = new LoadableDetachableModel<Depot>() {

			@Override
			protected Depot load() {
				Depot depot = state.rightSide.getDepot();
				if (state.leftSide.getDepot().equals(state.rightSide.getDepot()))
					depot.cacheObjectId(state.leftSide.getRevision(), leftCommitId);
				depot.cacheObjectId(state.rightSide.getRevision(), rightCommitId);
				return depot;
			}
			
		};
		WebMarkupContainer tabPanel;
		switch (state.tabPanel) {
		case CHANGES:
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
			
			tabPanel = new RevisionDiffPanel(TAB_PANEL_ID, depotModel, 
					new Model<PullRequest>(null), 
					state.compareWithMergeBase?mergeBaseModel.getObject():state.leftSide.getRevision(), 
					state.rightSide.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this);
			break;
		default:
			tabPanel = new CommitListPanel(TAB_PANEL_ID, depotModel, commitsModel);
			
			if (!mergeBaseModel.getObject().equals(leftCommitId.name()) && !state.compareWithMergeBase) {
				tabPanel.add(AttributeAppender.append("class", "with-merge-base"));
			}
			break;
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
		target.add(tabbable);
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
		
		public boolean compareWithMergeBase = true;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public TabPanel tabPanel;

		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Long commentId;

		@Nullable
		public DiffMark mark;
		
		public State() {
		}
		
		public State(State state) {
			leftSide = state.leftSide;
			rightSide = state.rightSide;
			compareWithMergeBase = state.compareWithMergeBase;
			whitespaceOption = state.whitespaceOption;
			pathFilter = state.pathFilter;
			blameFile = state.blameFile;
			commentId = state.commentId;
			mark = state.mark;
			tabPanel = state.tabPanel;
		}
		
	}

	@Override
	public DiffMark getMark() {
		return state.mark;
	}

	@Override
	public String getMarkUrl(DiffMark mark) {
		State markState = new State();
		markState.leftSide = new DepotAndRevision(state.rightSide.getDepot(), 
				state.compareWithMergeBase?mergeBaseModel.getObject():leftCommitId.name());
		markState.rightSide = new DepotAndRevision(state.rightSide.getDepot(), rightCommitId.name());
		markState.mark = mark;
		markState.pathFilter = state.pathFilter;
		markState.tabPanel = TabPanel.CHANGES;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, paramsOf(markState.rightSide.getDepot(), markState)).toString();
	}

	@Override
	public String getCommentUrl(CodeComment comment) {
		State commentState = new State();
		commentState.leftSide = new DepotAndRevision(state.rightSide.getDepot(), 
				state.compareWithMergeBase?mergeBaseModel.getObject():leftCommitId.name());
		commentState.rightSide = new DepotAndRevision(state.rightSide.getDepot(), rightCommitId.name());
		commentState.mark = new DiffMark(comment);
		commentState.commentId = comment.getId();
		commentState.tabPanel = TabPanel.CHANGES;
		commentState.pathFilter = state.pathFilter;
		commentState.whitespaceOption = state.whitespaceOption;
		commentState.compareWithMergeBase = false;
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
		if (comment != null) {
			state.mark = new DiffMark(comment);
			state.commentId = comment.getId();
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
	
}

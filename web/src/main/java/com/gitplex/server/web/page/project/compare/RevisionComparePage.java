package com.gitplex.server.web.page.project.compare;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.gitplex.server.GitPlex;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.manager.PullRequestManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.PullRequest;
import com.gitplex.server.model.support.MarkPos;
import com.gitplex.server.model.support.ProjectAndBranch;
import com.gitplex.server.model.support.ProjectAndRevision;
import com.gitplex.server.util.diff.WhitespaceOption;
import com.gitplex.server.web.behavior.TooltipBehavior;
import com.gitplex.server.web.component.commitlist.CommitListPanel;
import com.gitplex.server.web.component.diff.revision.MarkSupport;
import com.gitplex.server.web.component.diff.revision.RevisionDiffPanel;
import com.gitplex.server.web.component.link.ViewStateAwarePageLink;
import com.gitplex.server.web.component.revisionpicker.AffinalRevisionPicker;
import com.gitplex.server.web.component.tabbable.AjaxActionTab;
import com.gitplex.server.web.component.tabbable.Tab;
import com.gitplex.server.web.component.tabbable.Tabbable;
import com.gitplex.server.web.page.project.NoBranchesPage;
import com.gitplex.server.web.page.project.ProjectPage;
import com.gitplex.server.web.page.project.commit.CommitDetailPage;
import com.gitplex.server.web.page.project.pullrequest.newrequest.NewRequestPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.RequestDetailPage;
import com.gitplex.server.web.page.project.pullrequest.requestdetail.overview.RequestOverviewPage;
import com.gitplex.server.web.websocket.CommitIndexedRegion;
import com.gitplex.server.web.websocket.WebSocketManager;
import com.gitplex.server.web.websocket.WebSocketRegion;

@SuppressWarnings("serial")
public class RevisionComparePage extends ProjectPage implements MarkSupport {

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
	
	private static final String PARAM_MARK = "mark";
	
	private static final String PARAM_PATH_FILTER = "path-filter";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_TAB = "tab-panel";
	
	private static final String TABS_ID = "tabs";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private IModel<List<RevCommit>> commitsModel;
	
	private IModel<PullRequest> requestModel;
	
	private ObjectId mergeBase;

	private State state = new State();
	
	private ObjectId leftCommitId;
	
	private ObjectId rightCommitId;
	
	private Tabbable tabbable;
	
	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		params.set(PARAM_LEFT, state.leftSide.toString());
		params.set(PARAM_RIGHT, state.rightSide.toString());
		params.set(PARAM_COMPARE_WITH_MERGE_BASE, state.compareWithMergeBase);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.set(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.set(PARAM_PATH_FILTER, state.pathFilter);
		if (state.blameFile != null)
			params.set(PARAM_BLAME_FILE, state.blameFile);
		if (state.mark != null)
			params.set(PARAM_MARK, state.mark.toString());
		if (state.tabPanel != null)
			params.set(PARAM_TAB, state.tabPanel.name());
		return params;
	}

	public RevisionComparePage(PageParameters params) {
		super(params);
		
		if (getProject().getDefaultBranch() == null) 
			throw new RestartResponseException(NoBranchesPage.class, paramsOf(getProject()));

		String str = params.get(PARAM_LEFT).toString();
		if (str != null) {
			state.leftSide = new ProjectAndRevision(str);
		} else {
			state.leftSide = new ProjectAndRevision(getProject(), getProject().getDefaultBranch());
		}
		leftCommitId = state.leftSide.getCommit().copy();
		
		str = params.get(PARAM_RIGHT).toString();
		if (str != null) {
			state.rightSide = new ProjectAndRevision(str);
		} else {
			state.rightSide = new ProjectAndRevision(getProject(), getProject().getDefaultBranch());
		}
		rightCommitId = state.rightSide.getCommit().copy();
		
		state.compareWithMergeBase = params.get(PARAM_COMPARE_WITH_MERGE_BASE).toBoolean(true);
		
		/*
		 * When compare across different projects, left revision and right revision might not 
		 * exist in same project and this cause many difficulties such as calculating changes, 
		 * recording comment revisions, or get permanent mark urls. So we add below constraint as
		 * merge base commit and right side revision are guaranteed to be both in right side 
		 * project  
		 */
		if (!state.compareWithMergeBase && !state.leftSide.getProject().equals(state.rightSide.getProject())) {
			throw new IllegalArgumentException("Can only compare with common ancestor when different projects are involved");
		}
		
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.ofNullableName(params.get(PARAM_WHITESPACE_OPTION).toString());
		
		state.mark = MarkPos.fromString(params.get(PARAM_MARK).toString());
		
		state.tabPanel = TabPanel.of(params.get(PARAM_TAB).toString());
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				if (state.leftSide.getBranch() != null && state.rightSide.getBranch() != null) {
					ProjectAndBranch left = new ProjectAndBranch(state.leftSide.toString());
					ProjectAndBranch right = new ProjectAndBranch(state.rightSide.toString());
					return GitPlex.getInstance(PullRequestManager.class).findEffective(left, right);
				} else {
					return null;
				}
			}
			
		};

		try {
			Ref ref = state.rightSide.getProject().getRepository().findRef(state.rightSide.getRevision());
			String refName = ref!=null?ref.getName():null;
			mergeBase = GitUtils.getMergeBase(
					state.leftSide.getProject().getRepository(), leftCommitId, 
					state.rightSide.getProject().getRepository(), rightCommitId, 
					refName);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		commitsModel = new LoadableDetachableModel<List<RevCommit>>() {

			@Override
			protected List<RevCommit> load() {
				List<RevCommit> commits = new ArrayList<>();
				Project rightProject = state.rightSide.getProject();
				
				try (RevWalk revWalk = new RevWalk(state.rightSide.getProject().getRepository())) {
					if (rightProject.equals(state.leftSide.getProject()) 
							&& !state.compareWithMergeBase 
							&& !mergeBase.equals(leftCommitId)) {
						revWalk.markStart(revWalk.parseCommit(rightCommitId));
						revWalk.markStart(revWalk.parseCommit(leftCommitId));
						revWalk.markUninteresting(revWalk.parseCommit(mergeBase));
						revWalk.forEach(c->commits.add(c));
						/* 
						 * Add the merge base commit to make the revision graph understandable, 
						 * note that we can not get merge commit object in current revWalk as 
						 * it has been marked and this will make the commit object incomplete
						 */
						commits.add(getProject().getRevCommit(mergeBase));
					} else {
						revWalk.markStart(revWalk.parseCommit(rightCommitId));
						revWalk.markUninteresting(revWalk.parseCommit(mergeBase));
						revWalk.forEach(c->commits.add(c));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				return commits;
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		add(new AffinalRevisionPicker("leftRevSelector", state.leftSide.getProjectId(), state.leftSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String revision) {
				State newState = new State();
				newState.leftSide = new ProjectAndRevision(project, revision);
				newState.rightSide = state.rightSide;
				newState.pathFilter = state.pathFilter;
				newState.tabPanel = state.tabPanel;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = state.compareWithMergeBase;
				newState.mark = state.mark;
				newState.tabPanel = state.tabPanel;

				PageParameters params = paramsOf(project, newState);
				
				/*
				 * Use below code instead of calling setResponsePage() to make sure the dropdown is 
				 * closed while creating the new page as otherwise clicking other places in original page 
				 * while new page is loading will result in ComponentNotFound issue for the dropdown 
				 * component
				 */
				String url = RequestCycle.get().urlFor(RevisionComparePage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		
		CheckBox checkBox = new CheckBox("compareWithMergeBase", Model.of(state.compareWithMergeBase)) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(mergeBase != null && !mergeBase.equals(leftCommitId));
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
				newState.mark = state.mark;
				newState.tabPanel = state.tabPanel;
				
				PageParameters params = RevisionComparePage.paramsOf(projectModel.getObject(), newState);
				
				// Refer to comments in left revision picker for not using setResponsePage 
				String url = RequestCycle.get().urlFor(RevisionComparePage.class, params).toString();
				target.appendJavaScript(String.format("window.location.href='%s';", url));
			}
			
		});
		add(checkBox);

		String tooltip;
		if (!state.leftSide.getProject().equals(state.rightSide.getProject())) {
			checkBox.add(AttributeAppender.append("disabled", "disabled"));
			tooltip = "Can only compare with common ancestor when different projects are involved";
		} else {
			tooltip = "Check this to compare \"right side\" with common ancestor of left and right";
		}
		
		add(new WebMarkupContainer("mergeBaseTooltip").add(new TooltipBehavior(Model.of(tooltip))));

		PageParameters params = CommitDetailPage.paramsOf(state.leftSide.getProject(), state.leftSide.getCommit().name());
		Link<Void> leftCommitLink = new ViewStateAwarePageLink<Void>("leftCommitLink", CommitDetailPage.class, params);
		leftCommitLink.add(new Label("message", state.leftSide.getCommit().getShortMessage()));
		add(leftCommitLink);
		
		params = CommitDetailPage.paramsOf(state.rightSide.getProject(), state.rightSide.getCommit().name());
		Link<Void> rightCommitLink = new ViewStateAwarePageLink<Void>("rightCommitLink", CommitDetailPage.class, params);
		rightCommitLink.add(new Label("message", state.rightSide.getCommit().getShortMessage()));
		add(rightCommitLink);
		
		add(new AffinalRevisionPicker("rightRevSelector", 
				state.rightSide.getProjectId(), state.rightSide.getRevision()) { 

			@Override
			protected void onSelect(AjaxRequestTarget target, Project project, String revision) {
				State newState = new State();
				newState.leftSide = state.leftSide;
				newState.rightSide = new ProjectAndRevision(project, revision);
				newState.pathFilter = state.pathFilter;
				newState.tabPanel = state.tabPanel;
				newState.whitespaceOption = state.whitespaceOption;
				newState.compareWithMergeBase = state.compareWithMergeBase;
				newState.mark = state.mark;
				newState.tabPanel = state.tabPanel;
				
				PageParameters params = paramsOf(getProject(), newState);
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
				
				setResponsePage(RevisionComparePage.class,paramsOf(getProject(), newState));
			}

		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				ProjectAndBranch left = new ProjectAndBranch(state.leftSide.toString());
				ProjectAndBranch right = new ProjectAndBranch(state.rightSide.toString());
				setResponsePage(NewRequestPage.class, NewRequestPage.paramsOf(left.getProject(), left, right));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				if (mergeBase != null 
						&& getLoginUser() != null 
						&& state.leftSide.getBranch()!=null 
						&& state.rightSide.getBranch()!=null) {
					PullRequest request = requestModel.getObject();
					setVisible(request == null && !mergeBase.equals(rightCommitId));
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new WebMarkupContainer("effectiveRequest") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = requestModel.getObject();
				setVisible(request != null && (request.isOpen() || !request.isMergeIntoTarget()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new Label("description", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (requestModel.getObject().isOpen())
							return "This change is already opened for merge by a pull request";
						else 
							return "This change is squashed/rebased onto base branch via a pull request";
					}
					
				}));
				
				add(new Link<Void>("link") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								PullRequest request = requestModel.getObject();
								return "#" + request.getNumber() + " " + request.getTitle();
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
		
		add(new WebMarkupContainer("unrelatedHistory") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(mergeBase == null);
			}
			
		});
		
		if (mergeBase != null) {
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

			add(tabbable = new Tabbable(TABS_ID, tabs));

			newTabPanel(null);
		} else {
			add(new WebMarkupContainer(TABS_ID).setVisible(false));
			add(new WebMarkupContainer(TAB_PANEL_ID).setVisible(false));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RevisionCompareResourceReference()));
	}

	private void newTabPanel(@Nullable AjaxRequestTarget target) {
		IModel<Project> projectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				Project project = state.rightSide.getProject();
				if (state.leftSide.getProject().equals(state.rightSide.getProject()))
					project.cacheObjectId(state.leftSide.getRevision(), leftCommitId);
				project.cacheObjectId(state.rightSide.getRevision(), rightCommitId);
				return project;
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
			
			tabPanel = new RevisionDiffPanel(TAB_PANEL_ID, projectModel, 
					new Model<PullRequest>(null), 
					state.compareWithMergeBase?mergeBase.name():state.leftSide.getRevision(), 
					state.rightSide.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this);
			break;
		default:
			tabPanel = new CommitListPanel(TAB_PANEL_ID, projectModel, commitsModel);
			
			if (!mergeBase.equals(leftCommitId) && !state.compareWithMergeBase) {
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
		PageParameters params = paramsOf(getProject(), state);
		CharSequence url = RequestCycle.get().urlFor(RevisionComparePage.class, params);
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		
		state = (State) data;
		GitPlex.getInstance(WebSocketManager.class).onRegionChange(this);
		
		newTabPanel(target);
		target.add(tabbable);
	}
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

	public static class State implements Serializable {

		private static final long serialVersionUID = 1L;
		
		public ProjectAndRevision leftSide;
		
		public ProjectAndRevision rightSide;
		
		public boolean compareWithMergeBase = true;
		
		public WhitespaceOption whitespaceOption = WhitespaceOption.DEFAULT;
		
		@Nullable
		public TabPanel tabPanel;

		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public MarkPos mark;
		
		public State() {
		}
		
		public State(State state) {
			leftSide = state.leftSide;
			rightSide = state.rightSide;
			compareWithMergeBase = state.compareWithMergeBase;
			whitespaceOption = state.whitespaceOption;
			pathFilter = state.pathFilter;
			blameFile = state.blameFile;
			mark = state.mark;
			tabPanel = state.tabPanel;
		}
		
	}

	@Override
	public MarkPos getMark() {
		return state.mark;
	}
	
	@Override
	public String getMarkUrl(MarkPos mark) {
		State markState = new State();
		markState.leftSide = new ProjectAndRevision(state.rightSide.getProject(), 
				state.compareWithMergeBase?mergeBase.name():leftCommitId.name());
		markState.rightSide = new ProjectAndRevision(state.rightSide.getProject(), rightCommitId.name());
		markState.mark = mark;
		markState.pathFilter = state.pathFilter;
		markState.tabPanel = TabPanel.CHANGES;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, paramsOf(markState.rightSide.getProject(), markState)).toString();
	}

	@Override
	public Collection<WebSocketRegion> getWebSocketRegions() {
		Collection<WebSocketRegion> regions = super.getWebSocketRegions();
		if (state.compareWithMergeBase) 
			regions.add(new CommitIndexedRegion(mergeBase));
		else
			regions.add(new CommitIndexedRegion(state.leftSide.getCommit()));
		regions.add(new CommitIndexedRegion(state.rightSide.getCommit()));
		return regions;
	}

}

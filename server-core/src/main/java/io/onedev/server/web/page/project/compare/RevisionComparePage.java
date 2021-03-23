package io.onedev.server.web.page.project.compare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
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

import com.google.common.collect.Lists;

import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.OneDev;
import io.onedev.server.code.CodeProblem;
import io.onedev.server.code.CodeProblemContribution;
import io.onedev.server.code.LineCoverageContribution;
import io.onedev.server.entitymanager.CodeCommentManager;
import io.onedev.server.entitymanager.CodeCommentReplyManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectAndRevision;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.asset.revisioncompare.RevisionCompareCssResourceReference;
import io.onedev.server.web.component.commit.list.CommitListPanel;
import io.onedev.server.web.component.diff.revision.RevisionDiffPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.revisionpicker.AffinalRevisionPicker;
import io.onedev.server.web.component.tabbable.AjaxActionTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.PullRequestDetailPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.util.EditParamsAware;
import io.onedev.server.web.util.RevisionDiff;

@SuppressWarnings("serial")
public class RevisionComparePage extends ProjectPage implements RevisionDiff.AnnotationSupport, EditParamsAware {

	public enum TabPanel {
		COMMITS, 
		FILE_CHANGES;

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
	
	private static final String PARAM_COMMIT_QUERY = "commit-query";
	
	private static final String PARAM_BLAME_FILE = "blame-file";
	
	private static final String PARAM_TAB = "tab";
	
	private static final String TABS_ID = "tabs";
	
	private static final String TAB_PANEL_ID = "tabPanel";
	
	private final IModel<PullRequest> requestModel;
	
	private final IModel<Collection<CodeComment>> commentsModel = 
			new LoadableDetachableModel<Collection<CodeComment>>() {

		@Override
		protected Collection<CodeComment> load() {
			CodeCommentManager manager = OneDev.getInstance(CodeCommentManager.class);
			return manager.query(projectModel.getObject(), 
					state.compareWithMergeBase?mergeBase:leftCommitId, rightCommitId);
		}
		
	};
	
	private ObjectId mergeBase;

	private State state = new State();
	
	private ObjectId leftCommitId;
	
	private ObjectId rightCommitId;
	
	private Tabbable tabbable;

	public static RevisionComparePage.State getState(CodeComment comment) {
		RevisionComparePage.State state = new RevisionComparePage.State();
		state.commentId = comment.getId();
		state.mark = comment.getMark();
		state.compareWithMergeBase = false;
		CompareContext compareContext = comment.getCompareContext();
		String compareCommit = compareContext.getCompareCommitHash();
		Project project = comment.getProject();
		if (compareContext.isLeftSide()) {
			state.leftSide = new ProjectAndRevision(project, compareCommit);
			state.rightSide = new ProjectAndRevision(project, comment.getMark().getCommitHash());
		} else {
			state.leftSide = new ProjectAndRevision(project, comment.getMark().getCommitHash());
			state.rightSide = new ProjectAndRevision(project, compareCommit);
		}
		state.tabPanel = RevisionComparePage.TabPanel.FILE_CHANGES;
		state.whitespaceOption = compareContext.getWhitespaceOption();
		state.pathFilter = compareContext.getPathFilter();
		return state;
	}
	
	public static void fillParams(PageParameters params, State state) {
		if (state.leftSide.getRevision() != null)
			params.add(PARAM_LEFT, state.leftSide.toString());
		else
			params.add(PARAM_LEFT, state.leftSide.getProjectId());
		if (state.rightSide.getRevision() != null)
			params.add(PARAM_RIGHT, state.rightSide.toString());
		else
			params.add(PARAM_RIGHT, state.rightSide.getProjectId());
		params.add(PARAM_COMPARE_WITH_MERGE_BASE, state.compareWithMergeBase);
		if (state.whitespaceOption != WhitespaceOption.DEFAULT)
			params.add(PARAM_WHITESPACE_OPTION, state.whitespaceOption.name());
		if (state.pathFilter != null)
			params.add(PARAM_PATH_FILTER, state.pathFilter);
		if (state.commitQuery != null)
			params.add(PARAM_COMMIT_QUERY, state.commitQuery);
		if (state.blameFile != null)
			params.add(PARAM_BLAME_FILE, state.blameFile);
		if (state.commentId != null)
			params.add(PARAM_COMMENT, state.commentId);
		if (state.mark != null)
			params.add(PARAM_MARK, state.mark.toString());
		if (state.tabPanel != null)
			params.add(PARAM_TAB, state.tabPanel.name());
	}

	public static PageParameters paramsOf(Project project, State state) {
		PageParameters params = paramsOf(project);
		fillParams(params, state);
		return params;
	}

	public static PageParameters paramsOf(CodeComment comment) {
		return paramsOf(comment.getProject(), getState(comment));
	}
	
	public RevisionComparePage(PageParameters params) {
		super(params);
		
		String str = params.get(PARAM_LEFT).toString();
		if (str != null) 
			state.leftSide = new ProjectAndRevision(str);
		else if (getProject().getDefaultBranch() != null) 
			state.leftSide = new ProjectAndRevision(getProject(), getProject().getDefaultBranch());
		else
			state.leftSide = new ProjectAndRevision(getProject(), null);
		
		if (state.leftSide.getRevision() != null)
			leftCommitId = state.leftSide.getCommit().copy();
		
		str = params.get(PARAM_RIGHT).toString();
		if (str != null) 
			state.rightSide = new ProjectAndRevision(str);
		else if (getProject().getDefaultBranch() != null) 
			state.rightSide = new ProjectAndRevision(getProject(), getProject().getDefaultBranch());
		else
			state.rightSide = new ProjectAndRevision(getProject(), null);
		
		if (state.rightSide.getRevision() != null)
			rightCommitId = state.rightSide.getCommit().copy();
		
		state.compareWithMergeBase = params.get(PARAM_COMPARE_WITH_MERGE_BASE).toBoolean(true);
		
		/*
		 * When compare across different projects, left revision and right revision might not 
		 * exist in same project and this cause many difficulties such as calculating changes, 
		 * recording comment revisions, or get permanent mark urls. So we add below constraint as
		 * merge base commit and right side revision are guaranteed to be both in right side 
		 * project  
		 */
		if (!state.compareWithMergeBase && !state.leftSide.getProject().equals(state.rightSide.getProject())) 
			throw new IllegalArgumentException("Can only compare with common ancestor when different projects are involved");
		
		state.pathFilter = params.get(PARAM_PATH_FILTER).toString();
		state.blameFile = params.get(PARAM_BLAME_FILE).toString();
		state.whitespaceOption = WhitespaceOption.ofName(
				params.get(PARAM_WHITESPACE_OPTION).toString(WhitespaceOption.DEFAULT.name()));
		
		state.commitQuery = params.get(PARAM_COMMIT_QUERY).toString();
		
		state.commentId = params.get(PARAM_COMMENT).toOptionalLong();
		state.mark = Mark.fromString(params.get(PARAM_MARK).toString());
		
		state.tabPanel = TabPanel.of(params.get(PARAM_TAB).toString());
		
		requestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				if (state.leftSide.getBranch() != null && state.rightSide.getBranch() != null) {
					ProjectAndBranch left = new ProjectAndBranch(state.leftSide.toString());
					ProjectAndBranch right = new ProjectAndBranch(state.rightSide.toString());
					return OneDev.getInstance(PullRequestManager.class).findEffective(left, right);
				} else {
					return null;
				}
			}
			
		};

		if (leftCommitId != null && rightCommitId != null) {
			mergeBase = GitUtils.getMergeBase(
					state.leftSide.getProject().getRepository(), leftCommitId, 
					state.rightSide.getProject().getRepository(), rightCommitId);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
				newState.commentId = state.commentId;
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
				newState.commentId = state.commentId;
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
		
		add(new WebMarkupContainer("mergeBaseTooltip").add(AttributeAppender.append("title", tooltip)));

		if (state.leftSide.getRevision() != null) {
			PageParameters params = CommitDetailPage.paramsOf(state.leftSide.getProject(), state.leftSide.getCommit().name());
			Link<Void> leftCommitLink = new ViewStateAwarePageLink<Void>("leftCommitLink", CommitDetailPage.class, params);
			leftCommitLink.add(new Label("message", state.leftSide.getCommit().getShortMessage()));
			add(leftCommitLink);
		} else {
			WebMarkupContainer leftCommitLink = new WebMarkupContainer("leftCommitLink");
			leftCommitLink.add(new WebMarkupContainer("message"));
			add(leftCommitLink.setVisible(false));
		}

		if (state.rightSide.getRevision() != null) {
			PageParameters params = CommitDetailPage.paramsOf(state.rightSide.getProject(), state.rightSide.getCommit().name());
			Link<Void> rightCommitLink = new ViewStateAwarePageLink<Void>("rightCommitLink", CommitDetailPage.class, params);
			rightCommitLink.add(new Label("message", state.rightSide.getCommit().getShortMessage()));
			add(rightCommitLink);
		} else {
			WebMarkupContainer rightCommitLink = new WebMarkupContainer("rightCommitLink");
			rightCommitLink.add(new WebMarkupContainer("message"));
			add(rightCommitLink.setVisible(false));
		}
		
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
				newState.commentId = state.commentId;
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
				newState.commentId = state.commentId;
				newState.mark = state.mark;
				
				setResponsePage(RevisionComparePage.class,paramsOf(getProject(), newState));
			}

		});
		
		add(new Link<Void>("createRequest") {

			@Override
			public void onClick() {
				ProjectAndBranch left = new ProjectAndBranch(state.leftSide.toString());
				ProjectAndBranch right = new ProjectAndBranch(state.rightSide.toString());
				setResponsePage(NewPullRequestPage.class, NewPullRequestPage.paramsOf(left.getProject(), left, right));
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
				setVisible(request != null && (request.isOpen() || !request.isMergedIntoTarget()));
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new Label("description", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (requestModel.getObject().isOpen())
							return "This change is already opened for merge by ";
						else 
							return "This change is squashed/rebased onto base branch via ";
					}
					
				}).setEscapeModelStrings(false));
				
				add(new Link<Void>("link") {

					@Override
					protected void onInitialize() {
						super.onInitialize();
						add(new Label("label", new AbstractReadOnlyModel<String>() {

							@Override
							public String getObject() {
								return "pull request #" + requestModel.getObject().getNumber();
							}
							
						}));
					}

					@Override
					public void onClick() {
						PageParameters params = PullRequestDetailPage.paramsOf(requestModel.getObject());
						setResponsePage(PullRequestActivitiesPage.class, params);
					}
					
				});
				
			}
			
		});
		
		add(new WebMarkupContainer("revisionNotSpecified") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(leftCommitId == null || rightCommitId == null);
			}
			
		});
		
		add(new WebMarkupContainer("unrelatedHistory") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(leftCommitId != null && rightCommitId != null && mergeBase == null);
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
					resizeWindow(target);
				}
				
			});

			tabs.add(new AjaxActionTab(Model.of("File Changes")) {
				
				@Override
				public boolean isSelected() {
					return state.tabPanel == TabPanel.FILE_CHANGES;
				}
				
				@Override
				protected void onSelect(AjaxRequestTarget target, Component tabLink) {
					state.tabPanel = TabPanel.FILE_CHANGES;
					newTabPanel(target);
					pushState(target);
					resizeWindow(target);
				}
				
			});

			add(tabbable = new Tabbable(TABS_ID, tabs));

			newTabPanel(null);
		} else {
			add(new WebMarkupContainer(TABS_ID).setVisible(false));
			add(new WebMarkupContainer(TAB_PANEL_ID).setVisible(false));
		}
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new RevisionCompareCssResourceReference()));
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
		case FILE_CHANGES:
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
			
			tabPanel = new RevisionDiffPanel(TAB_PANEL_ID, 
					state.compareWithMergeBase?mergeBase.name():state.leftSide.getRevision(), 
					state.rightSide.getRevision(), pathFilterModel, whitespaceOptionModel, blameModel, this) {
				
				@Override
				protected Project getProject() {
					return projectModel.getObject();
				}

			};
			break;
		default:
			tabPanel = new CommitListPanel(TAB_PANEL_ID, new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return state.commitQuery;
				}

				@Override
				public void setObject(String object) {
					state.commitQuery = object;
					pushState(RequestCycle.get().find(AjaxRequestTarget.class));
				}
				
			}) {

				@Override
				protected CommitQuery getBaseQuery() {
					List<Revision> revisions = new ArrayList<>();
					
					revisions.add(new Revision(mergeBase.name(), Revision.Scope.SINCE));
					revisions.add(new Revision(rightCommitId.name(), Revision.Scope.UNTIL));
					
					Project rightProject = state.rightSide.getProject();
					if (rightProject.equals(state.leftSide.getProject()) 
							&& !state.compareWithMergeBase 
							&& !mergeBase.equals(leftCommitId)) {
						revisions.add(new Revision(leftCommitId.name(), Revision.Scope.UNTIL));
					} 
					return new CommitQuery(Lists.newArrayList(new RevisionCriteria(revisions)));
				}

				@Override
				protected Project getProject() {
					return RevisionComparePage.this.getProject();
				}
				
			};
			
			if (!mergeBase.equals(leftCommitId) && !state.compareWithMergeBase) 
				tabPanel.add(AttributeAppender.append("class", "with-merge-base"));
			
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
		newTabPanel(target);
		target.add(tabbable);
	}
	
	@Override
	protected void onDetach() {
		if (getProject().getDefaultBranch() != null)
			requestModel.detach();
		commentsModel.detach();
		super.onDetach();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
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
		public String commitQuery;

		@Nullable
		public String pathFilter;
		
		@Nullable
		public String blameFile;
		
		@Nullable
		public Long commentId;
		
		@Nullable
		public Mark mark;
		
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
	public Mark getMark() {
		return state.mark;
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
		state.mark = comment.getMark();
		state.commentId = comment.getId();
		pushState(target);
	}

	@Override
	public void onCommentClosed(AjaxRequestTarget target) {
		state.commentId = null;
		state.mark = null;
		pushState(target);
	}
	
	@Override
	protected String getRobotsMeta() {
		return "noindex,nofollow";
	}
	
	@Override
	public void onMark(AjaxRequestTarget target, Mark mark) {
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onUnmark(AjaxRequestTarget target) {
		state.mark = null;
		pushState(target);
	}
	
	@Override
	public void onAddComment(AjaxRequestTarget target, Mark mark) {
		state.commentId = null;
		state.mark = mark;
		pushState(target);
	}

	@Override
	public void onSaveComment(CodeComment comment) {
		OneDev.getInstance(CodeCommentManager.class).save(comment);
	}
	
	@Override
	public void onSaveCommentReply(CodeCommentReply reply) {
		OneDev.getInstance(CodeCommentReplyManager.class).save(reply);
	}

	@Override
	public String getMarkUrl(Mark mark) {
		State markState = new State();
		markState.leftSide = new ProjectAndRevision(state.rightSide.getProject(), 
				state.compareWithMergeBase?mergeBase.name():leftCommitId.name());
		markState.rightSide = new ProjectAndRevision(state.rightSide.getProject(), rightCommitId.name());
		markState.mark = mark;
		markState.pathFilter = state.pathFilter;
		markState.tabPanel = TabPanel.FILE_CHANGES;
		markState.whitespaceOption = state.whitespaceOption;
		markState.compareWithMergeBase = false;
		return urlFor(RevisionComparePage.class, paramsOf(markState.rightSide.getProject(), markState)).toString();
	}

	@Override
	public PageParameters getParamsBeforeEdit() {
		return paramsOf(getProject(), state);
	}

	@Override
	public PageParameters getParamsAfterEdit() {
		return paramsOf(getProject(), state);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Code Compare");
	}

	@Override
	public Map<CodeComment, PlanarRange> getOldComments(String blobPath) {
		Map<CodeComment, PlanarRange> oldComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			ObjectId oldCommitId = state.compareWithMergeBase?mergeBase:leftCommitId;
			if (comment.getMark().getCommitHash().equals(oldCommitId.name()) 
					&& comment.getMark().getPath().equals(blobPath)) {
				oldComments.put(comment, comment.getMark().getRange());
			}
		}
		return oldComments;
	}

	@Override
	public Map<CodeComment, PlanarRange> getNewComments(String blobPath) {
		Map<CodeComment, PlanarRange> newComments = new HashMap<>();
		for (CodeComment comment: commentsModel.getObject()) {
			if (comment.getMark().getCommitHash().equals(rightCommitId.name())
					&& comment.getMark().getPath().equals(blobPath)) {
				newComments.put(comment, comment.getMark().getRange());
			}
		}
		return newComments;
	}

	@Override
	public Collection<CodeProblem> getOldProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		ObjectId oldCommitId = state.compareWithMergeBase?mergeBase:leftCommitId;
		for (Build build: getBuilds(oldCommitId)) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Collection<CodeProblem> getNewProblems(String blobPath) {
		Set<CodeProblem> problems = new HashSet<>();
		for (Build build: getBuilds(rightCommitId)) {
			for (CodeProblemContribution contribution: OneDev.getExtensions(CodeProblemContribution.class))
				problems.addAll(contribution.getCodeProblems(build, blobPath, null));
		}
		return problems;
	}

	@Override
	public Map<Integer, Integer> getOldCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		ObjectId oldCommitId = state.compareWithMergeBase?mergeBase:leftCommitId;
		for (Build build: getBuilds(oldCommitId)) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}

	@Override
	public Map<Integer, Integer> getNewCoverages(String blobPath) {
		Map<Integer, Integer> coverages = new HashMap<>();
		for (Build build: getBuilds(rightCommitId)) {
			for (LineCoverageContribution contribution: OneDev.getExtensions(LineCoverageContribution.class)) {
				contribution.getLineCoverages(build, blobPath, null).forEach((key, value) -> {
					coverages.merge(key, value, (v1, v2) -> v1+v2);
				});
			}
		}
		return coverages;
	}
	
	@Override
	protected String getPageTitle() {
		return "Code Compare - " + getProject().getName();
	}
	
}

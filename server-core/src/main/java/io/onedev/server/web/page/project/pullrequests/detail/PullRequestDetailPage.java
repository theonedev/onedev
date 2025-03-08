package io.onedev.server.web.page.project.pullrequests.detail;

import static io.onedev.server.entityreference.ReferenceUtils.transformReferences;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.CREATE_MERGE_COMMIT_IF_NECESSARY;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.REBASE_SOURCE_BRANCH_COMMITS;
import static io.onedev.server.model.support.pullrequest.MergeStrategy.SQUASH_SOURCE_BRANCH_COMMITS;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.entitymanager.PullRequestAssignmentManager;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.entitymanager.PullRequestLabelManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.PullRequestReactionManager;
import io.onedev.server.entitymanager.PullRequestReviewManager;
import io.onedev.server.entitymanager.PullRequestWatchManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entityreference.EntityReference;
import io.onedev.server.entityreference.LinkTransformer;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.GitService;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAssignment;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.model.PullRequestReview;
import io.onedev.server.model.PullRequestReview.Status;
import io.onedev.server.model.PullRequestWatch;
import io.onedev.server.model.User;
import io.onedev.server.model.support.EntityReaction;
import io.onedev.server.model.support.EntityWatch;
import io.onedev.server.model.support.code.BuildRequirement;
import io.onedev.server.model.support.pullrequest.AutoMerge;
import io.onedev.server.model.support.pullrequest.MergePreview;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.pullrequest.PullRequestQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.asset.emoji.Emojis;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.behavior.ReferenceInputBehavior;
import io.onedev.server.web.component.MultilineLabel;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.entity.reference.EntityReferencePanel;
import io.onedev.server.web.component.entity.watches.EntityWatchesPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.RunJobLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.project.gitprotocol.GitProtocolPanel;
import io.onedev.server.web.component.pullrequest.assignment.AssignmentListPanel;
import io.onedev.server.web.component.pullrequest.build.PullRequestJobsPanel;
import io.onedev.server.web.component.pullrequest.review.ReviewListPanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTab;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.compare.RevisionComparePage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.pullrequests.InvalidPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.ProjectPullRequestsPage;
import io.onedev.server.web.page.project.pullrequests.create.NewPullRequestPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.changes.PullRequestChangesPage;
import io.onedev.server.web.page.project.pullrequests.detail.codecomments.PullRequestCodeCommentsPage;
import io.onedev.server.web.page.project.pullrequests.detail.operationdlg.MergePullRequestOptionPanel;
import io.onedev.server.web.page.project.pullrequests.detail.operationdlg.OperationCommentPanel;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.PullRequestAware;
import io.onedev.server.web.util.editbean.CommitMessageBean;
import io.onedev.server.web.util.editbean.LabelsBean;
import io.onedev.server.xodus.VisitInfoManager;

public abstract class PullRequestDetailPage extends ProjectPage implements PullRequestAware {

	public static final String PARAM_REQUEST = "request";

	protected final IModel<PullRequest> requestModel;

	private boolean isEditingTitle;

	private String title;

	private Long latestUpdateId;

	private MergeStrategy mergeStrategy;

	public PullRequestDetailPage(PageParameters params) {
		super(params);

		String requestNumberString = params.get(PARAM_REQUEST).toString();
		if (StringUtils.isBlank(requestNumberString)) {
			throw new RestartResponseException(ProjectPullRequestsPage.class,
					ProjectPullRequestsPage.paramsOf(getProject(), null, 0));
		}

		requestModel = new LoadableDetachableModel<>() {

			@Override
			protected PullRequest load() {
				Long requestNumber;
				try {
					requestNumber = Long.valueOf(requestNumberString);
				} catch (NumberFormatException e) {
					throw new ValidationException("Invalid pull request number: " + requestNumberString);
				}

				PullRequest request = getPullRequestManager().find(getProject(), requestNumber);
				if (request == null) {
					throw new EntityNotFoundException("Unable to find pull request #"
							+ requestNumber + " in project " + getProject());
				} else if (!request.getTargetProject().equals(getProject())) {
					throw new RestartResponseException(getPageClass(), paramsOf(request));
				} else {
					return request;
				}
			}

		};

		if (!getPullRequest().isValid()) {
			throw new RestartResponseException(InvalidPullRequestPage.class,
					InvalidPullRequestPage.paramsOf(getPullRequest()));
		}

		latestUpdateId = requestModel.getObject().getLatestUpdate().getId();
	}

	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
	}

	private PullRequestManager getPullRequestManager() {
		return OneDev.getInstance(PullRequestManager.class);
	}

	private PullRequestReviewManager getPullRequestReviewManager() {
		return OneDev.getInstance(PullRequestReviewManager.class);
	}

	private PullRequestChangeManager getPullRequestChangeManager() {
		return OneDev.getInstance(PullRequestChangeManager.class);
	}

	private WebMarkupContainer newRequestHead() {
		WebMarkupContainer requestHead = new WebMarkupContainer("requestHeader");
		requestHead.setOutputMarkupId(true);
		add(requestHead);

		requestHead.add(new Label("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequest request = getPullRequest();
				var transformed = transformReferences(request.getTitle(), request.getTargetProject(),
						new LinkTransformer(null));
				transformed = Emojis.getInstance().apply(transformed);
				return transformed + " (" + getPullRequest().getReference().toString(getProject()) + ")";
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!isEditingTitle);
			}

		}.setEscapeModelStrings(false));

		requestHead.add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isEditingTitle = true;

				target.add(requestHead);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!isEditingTitle && SecurityUtils.canModifyPullRequest(getPullRequest()));
			}

		});

		Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(isEditingTitle);
			}

		};
		form.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				if (form.hasError())
					return "is-invalid";
				else
					return "";
			}

		}));
		requestHead.add(form);

		title = getPullRequest().getTitle();
		form.add(new TextField<>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return title;
			}

			@Override
			public void setObject(String object) {
				title = object;
			}

		}).setRequired(true).add(new ReferenceInputBehavior() {

			@Override
			protected Project getProject() {
				return PullRequestDetailPage.this.getProject();
			}

		}));

		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				OneDev.getInstance(PullRequestChangeManager.class).changeTitle(getPullRequest(), title);
				notifyPullRequestChange(target);
				isEditingTitle = false;

				target.add(requestHead);
				resizeWindow(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(requestHead);
			}

		});

		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isEditingTitle = false;
				target.add(requestHead);
				resizeWindow(target);
			}

		});

		requestHead.add(new SideInfoLink("moreInfo"));

		return requestHead;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(newRequestHead());
		add(newStatusBarContainer());
		add(newDescriptionContainer());

		WebMarkupContainer summaryContainer = new WebMarkupContainer("requestSummary") {

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.pullRequestDetail.onSummaryDomReady();"));
			}

		};

		summaryContainer.add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
			}

		});
		summaryContainer.setOutputMarkupId(true);
		add(summaryContainer);

		summaryContainer.add(new Label("checkError", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getCheckError();
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getCheckError() != null);
			}

		});

		summaryContainer.add(new WebMarkupContainer("discarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isDiscarded());
			}

		});

		summaryContainer.add(new WebMarkupContainer("fastForwarded") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				MergePreview preview = request.getMergePreview();
				setVisible(request.isMerged() && preview != null
						&& preview.getHeadCommitHash().equals(preview.getMergeCommitHash()));
			}

		});
		summaryContainer.add(new WebMarkupContainer("merged") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				MergePreview preview = request.getMergePreview();
				setVisible(request.isMerged() && preview != null
						&& (preview.getMergeStrategy() == CREATE_MERGE_COMMIT
								|| preview.getMergeStrategy() == CREATE_MERGE_COMMIT_IF_NECESSARY
										&& !preview.getHeadCommitHash().equals(preview.getMergeCommitHash())));
			}

		});
		summaryContainer.add(new WebMarkupContainer("mergedOutside") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(getPullRequest().isMerged() && getPullRequest().getMergePreview() == null);
			}

		});
		summaryContainer.add(new WebMarkupContainer("squashed") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				MergePreview preview = request.getMergePreview();
				setVisible(request.isMerged() && preview != null
						&& preview.getMergeStrategy() == SQUASH_SOURCE_BRANCH_COMMITS);
			}

		});
		summaryContainer.add(new WebMarkupContainer("rebased") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				MergePreview preview = request.checkMergePreview();
				setVisible(request.isMerged() && preview != null
						&& preview.getMergeStrategy() == REBASE_SOURCE_BRANCH_COMMITS);
			}

		});

		summaryContainer.add(new WebMarkupContainer("calculatingMergePreview") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen() && getPullRequest().checkMergePreview() == null);
			}

		});
		summaryContainer.add(new WebMarkupContainer("sourceBranchOutdated") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Link<Void>("detail") {

					@Override
					public void onClick() {
						RevisionComparePage.State state = new RevisionComparePage.State();
						var request = getPullRequest();
						state.leftSide = request.getSource();
						state.rightSide = request.getTarget();
						PageParameters params = RevisionComparePage.paramsOf(getProject(), state);
						setResponsePage(RevisionComparePage.class, params);
					}

				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen()
						&& getPullRequest().getSourceHead() != null
						&& getPullRequest().isSourceOutdated());
			}
		});

		summaryContainer.add(new WebMarkupContainer("hasMergeConflict") {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new DropdownLink("resolveInstructions") {

					@Override
					protected void onInitialize(FloatingPanel dropdown) {
						dropdown.add(AttributeAppender.append("class", "conflict-resolve-instruction"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().getSource() != null);
					}

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						if (getPullRequest().getTargetProject().equals(getPullRequest().getSourceProject())) {
							return new ConflictResolveInstructionPanel(id) {

								@Override
								protected PullRequest getPullRequest() {
									return PullRequestDetailPage.this.getPullRequest();
								}

							};
						} else {
							return new GitProtocolPanel(id) {

								@Override
								protected Component newContent(String componentId) {
									return new ConflictResolveInstructionPanel(componentId) {

										@Override
										protected PullRequest getPullRequest() {
											return PullRequestDetailPage.this.getPullRequest();
										}

									};
								}

								@Override
								protected Project getProject() {
									return getPullRequest().getTargetProject();
								}

							};
						}
					}

				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().checkMergePreview();
				setVisible(getPullRequest().isOpen()
						&& preview != null
						&& preview.getMergeCommitHash() == null);
			}

		});
		summaryContainer.add(new WebMarkupContainer("calculatedMergePreview") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				MergePreview preview = getPullRequest().checkMergePreview();
				setVisible(getPullRequest().isOpen()
						&& preview != null
						&& preview.getMergeCommitHash() != null);
			}

		});

		summaryContainer.add(new WebMarkupContainer("noValidCommitSignature") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen() && !getPullRequest().isSignatureRequirementSatisfied());
			}

		});
		summaryContainer.add(new WebMarkupContainer("requestedForChanges") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen() && requestedForChanges());
			}

		});
		summaryContainer.add(new WebMarkupContainer("waitingForReviews") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(getPullRequest().isOpen() && !requestedForChanges()
						&& getPullRequest().getReviews().stream().anyMatch(it-> it.getStatus()==Status.PENDING));
			}

		});
		summaryContainer.add(new WebMarkupContainer("unsuccessfulBuilds") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					BuildRequirement buildRequirement = request.getBuildRequirement();
					if (buildRequirement.isStrictMode() && request.isBuildCommitOutdated()) {
						setVisible(false);
					} else {
						var requiredJobs = buildRequirement.getRequiredJobs();
						setVisible(request.getCurrentBuilds().stream().anyMatch(
								it-> requiredJobs.contains(it.getJobName()) && it.isFinished() && !it.isSuccessful()));
					}
				} else {
					setVisible(false);
				}
			}

		});
		summaryContainer.add(new WebMarkupContainer("unfinishedBuilds") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					BuildRequirement buildRequirement = request.getBuildRequirement();
					if (buildRequirement.isStrictMode() && request.isBuildCommitOutdated()) {
						setVisible(false);
					} else {
						var requiredJobs = buildRequirement.getRequiredJobs();
						setVisible(request.getCurrentBuilds().stream().anyMatch(
								it-> requiredJobs.contains(it.getJobName()) && !it.isFinished()));
					}
				} else {
					setVisible(false);
				}
			}

		});

		summaryContainer.add(new Label("commitMessageCheckError", new LoadableDetachableModel<>() {
			@Override
			protected String load() {
				var commitMessageError = getPullRequest().checkCommitMessages();
				if (commitMessageError != null) {
					if (commitMessageError.getCommitId() != null) {
						var params = CommitDetailPage.paramsOf(getProject(), commitMessageError.getCommitId().name());
						return String.format("Error validating commit message of <a href='%s' class='text-monospace font-size-sm'>%s</a>: %s",
								RequestCycle.get().urlFor(CommitDetailPage.class, params),
								GitUtils.abbreviateSHA(commitMessageError.getCommitId().name()),
								escapeHtml5(commitMessageError.getErrorMessage()));
					} else {
						return String.format("Error validating auto merge commit message: %s",
								escapeHtml5(commitMessageError.getErrorMessage()));
					}
				} else {
					return null;
				}
			}
		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				if (request.isOpen())
					setVisible(getDefaultModelObject() != null);
				else
					setVisible(false);
			}
		}.setEscapeModelStrings(false));

		summaryContainer.add(new Label("requiredJobsMessage", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				if (getPullRequest().getBuildRequirement().isStrictMode())
					return "Jobs required to be successful on merge commit: ";
				else
					return "Jobs required to be successful: ";
			}
		}));
		summaryContainer.add(new ListView<String>("requiredJobs", new LoadableDetachableModel<List<String>>() {
			@Override
			protected List<String> load() {
				PullRequest request = getPullRequest();
				BuildRequirement buildRequirement = request.getBuildRequirement();
				List<String> requiredJobs = new ArrayList<>(buildRequirement.getRequiredJobs());
				if (!buildRequirement.isStrictMode() || !request.isBuildCommitOutdated()) {
					for (Build build: request.getCurrentBuilds())
						requiredJobs.remove(build.getJobName());
				}
				return requiredJobs;
			}

		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				String jobName = item.getModelObject();
				item.add(new Label("jobName", jobName));

				PullRequest request = getPullRequest();
				String commitHash;
				if (request.getBuildRequirement().isStrictMode() || request.getBuildCommitHash() == null)
					commitHash = request.checkMergePreview().getMergeCommitHash();
				else
					commitHash = request.getBuildCommitHash();

				item.add(new RunJobLink("runJob",
						ObjectId.fromString(commitHash), jobName, request.getMergeRef()) {

					@Override
					protected Project getProject() {
						return PullRequestDetailPage.this.getProject();
					}

					@Nullable
					@Override
					protected PullRequest getPullRequest() {
						return PullRequestDetailPage.this.getPullRequest();
					}

					@Override
					protected void onConfigure() {
						setVisible(SecurityUtils.canRunJob(getProject(), jobName)
								|| SecurityUtils.canModifyPullRequest(getPullRequest()));
					}
				});
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();

				MergePreview mergePreview = getPullRequest().checkMergePreview();
				setVisible(getPullRequest().isOpen()
						&& mergePreview != null
						&& mergePreview.getMergeCommitHash() != null
						&& !getModelObject().isEmpty());
			}

		});

		summaryContainer.add(new WebMarkupContainer("mergeableByCodeWriters") {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				MergePreview mergePreview = getPullRequest().checkMergePreview();
				setVisible(getPullRequest().isOpen() && !SecurityUtils.canWriteCode(getProject())
						&& mergePreview != null && mergePreview.getMergeCommitHash() != null
						&& getPullRequest().isAllReviewsApproved() && getPullRequest().isBuildRequirementSatisfied());
			}

		});

		summaryContainer.add(newSummaryContributions());

		add(newOperationsContainer());
		add(newMoreInfoContainer());

		List<Tab> tabs = new ArrayList<>();

		tabs.add(new PageTab(Model.of("Activities"), PullRequestActivitiesPage.class, PullRequestActivitiesPage.paramsOf(getPullRequest())) {

			@Override
			protected Component renderOptions(String componentId) {
				PullRequestActivitiesPage page = (PullRequestActivitiesPage) getPage();
				return page.renderOptions(componentId);
			}

		});
		tabs.add(new PageTab(Model.of("File Changes"), PullRequestChangesPage.class, PullRequestChangesPage.paramsOf(getPullRequest())));
		tabs.add(new PageTab(Model.of("Code Comments"), PullRequestCodeCommentsPage.class, PullRequestCodeCommentsPage.paramsOf(getPullRequest())) {
			@Override
			public Component render(String componentId) {
				Fragment fragment = new Fragment(componentId, "codeCommentsTabLinkFrag", PullRequestDetailPage.this);

				// Do not show unresolved only by default as new indicator for
				// code comments tab may also be caused by new activities in
				// resolved issues
				Link<Void> link = new ViewStateAwarePageLink<Void>("link",
						PullRequestCodeCommentsPage.class,
						PullRequestCodeCommentsPage.paramsOf(getPullRequest()));
				link.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
					@Override
					protected String load() {
						Date updateDate = getPullRequest().getCodeCommentsUpdateDate();
						if (updateDate != null && !getPullRequest().isCodeCommentsVisitedAfter(updateDate))
							return "new";
						else
							return "";
					}

				}));
				link.add(new ChangeObserver() {

					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
					}

				});
				link.setOutputMarkupId(true);
				fragment.add(link);
				return fragment;
			}
		});

		add(new Tabbable("requestTabs", tabs).setOutputMarkupId(true));

		RequestCycle.get().getListeners().add(new IRequestCycleListener() {

			@Override
			public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
			}

			@Override
			public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
			}

			@Override
			public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
			}

			@Override
			public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
			}

			@Override
			public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
			}

			@Override
			public IRequestHandler onException(RequestCycle cycle, Exception ex) {
				return null;
			}

			@Override
			public void onEndRequest(RequestCycle cycle) {
				if (SecurityUtils.getAuthUser() != null)
					OneDev.getInstance(VisitInfoManager.class).visitPullRequest(SecurityUtils.getAuthUser(), getPullRequest());
			}

			@Override
			public void onDetach(RequestCycle cycle) {
			}

			@Override
			public void onBeginRequest(RequestCycle cycle) {
			}

		});
	}

	private boolean requestedForChanges() {
		return getPullRequest().getReviews().stream()
				.anyMatch(it-> it.getStatus() == Status.REQUESTED_FOR_CHANGES);
	}

	private WebMarkupContainer newMoreInfoContainer() {
		return new SideInfoPanel("moreInfo") {

			@Override
			protected Component newBody(String componentId) {
				Fragment fragment = new Fragment(componentId, "moreInfoFrag", PullRequestDetailPage.this) {

					@Override
					protected void onBeforeRender() {
						replace(new BranchLink("targetBranch", getPullRequest().getTarget()));
						super.onBeforeRender();
					}

				};

				fragment.add(new UserIdentPanel("submitter", getPullRequest().getSubmitter(), Mode.NAME));
				fragment.add(new BranchLink("targetBranch", getPullRequest().getTarget()));

				fragment.add(new MenuLink("changeTargetBranch") {

					@Override
					protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
						List<MenuItem> menuItems = new ArrayList<>();
						Project project = getProject();
						for (RefFacade ref: project.getBranchRefs()) {
							String branch = GitUtils.ref2branch(ref.getName());
							PullRequest request = getPullRequest();
							if (!branch.equals(request.getTargetBranch()) &&
									(!project.equals(request.getSourceProject()) || !branch.equals(request.getSourceBranch()))) {
								menuItems.add(new MenuItem() {

									@Override
									public String getLabel() {
										return branch;
									}

									@Override
									public WebMarkupContainer newLink(String id) {
										return new AjaxLink<Void>(id) {

											@Override
											protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
												super.updateAjaxAttributes(attributes);
												attributes.getAjaxCallListeners().add(new ConfirmClickListener(
														"Do you really want to change target branch to " + branch + "?"));
											}

											@Override
											public void onClick(AjaxRequestTarget target) {
												OneDev.getInstance(PullRequestChangeManager.class).changeTargetBranch(getPullRequest(), branch);
												notifyPullRequestChange(target);
												dropdown.close();
											}

										};
									}

								});
							}
						}

						return menuItems;
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest()));
					}

				});

				if (getPullRequest().getSourceProject() != null) {
					fragment.add(new BranchLink("sourceBranch", getPullRequest().getSource()));
				} else {
					fragment.add(new Label("sourceBranch", "unknown") {

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("em");
						}

					});
				}

				fragment.add(newMergeStrategyContainer());
				fragment.add(newAutoMergeContainer());
				fragment.add(new ReviewListPanel("reviews") {

					@Override
					protected PullRequest getPullRequest() {
						return PullRequestDetailPage.this.getPullRequest();
					}

				});
				fragment.add(new WebMarkupContainer("reviewerHelp") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest()));
					}

				});
				fragment.add(new WebMarkupContainer("hiddenJobsNote") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						boolean hasHiddenJobs = false;
						for (String jobName: getPullRequest().getCurrentBuilds().stream()
								.map(it->it.getJobName()).collect(Collectors.toSet())) {
							if (!SecurityUtils.canAccessJob(getProject(), jobName)) {
								hasHiddenJobs = true;
								break;
							}
						}
						setVisible(hasHiddenJobs);
					}

				});
				fragment.add(new PullRequestJobsPanel("jobs") {

					@Override
					protected PullRequest getPullRequest() {
						return PullRequestDetailPage.this.getPullRequest();
					}

				});
				fragment.add(new WebMarkupContainer("jobsHelp") {

					@Override
					protected void onConfigure() {
						super.onConfigure();

						boolean hasVisibleRequiredJobs = false;
						for (Build build: getPullRequest().getCurrentBuilds()) {
							if (getPullRequest().getBuildRequirement().getRequiredJobs().contains(build.getJobName())
									&& SecurityUtils.canAccessJob(getProject(), build.getJobName())) {
								hasVisibleRequiredJobs = true;
								break;
							}
						}
						setVisible(hasVisibleRequiredJobs);
					}

				});
				fragment.add(new AjaxLink<Void>("assignToMe") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						var assignment = new PullRequestAssignment();
						assignment.setRequest(getPullRequest());
						assignment.setUser(SecurityUtils.getUser());
						OneDev.getInstance(PullRequestAssignmentManager.class).create(assignment);
						((BasePage)getPage()).notifyObservableChange(target,
								PullRequest.getChangeObservable(getPullRequest().getId()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().isOpen()
								&& SecurityUtils.getUser() != null
								&& !getPullRequest().getAssignees().contains(SecurityUtils.getUser())
								&& SecurityUtils.canWriteCode(getProject())
								&& SecurityUtils.canModifyPullRequest(getPullRequest()));
					}
				});
				fragment.add(new AssignmentListPanel("assignments") {

					@Override
					protected PullRequest getPullRequest() {
						return PullRequestDetailPage.this.getPullRequest();
					}

				});
				fragment.add(new WebMarkupContainer("assigneeHelp") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest()));
					}

				});

				WebMarkupContainer labelsContainer = new WebMarkupContainer("labels") {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!getPullRequest().getLabels().isEmpty() || SecurityUtils.canModifyPullRequest(getPullRequest()));
					}

				};
				labelsContainer.setOutputMarkupId(true);

				if (SecurityUtils.canModifyPullRequest(getPullRequest())) {
					labelsContainer.add(new InplacePropertyEditLink("head") {

						@Override
						protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
							LabelsBean labelsBean = (LabelsBean) bean;
							OneDev.getInstance(PullRequestLabelManager.class).sync(getPullRequest(), labelsBean.getLabels());
							handler.add(labelsContainer);
						}

						@Override
						protected String getPropertyName() {
							return "labels";
						}

						@Override
						protected Project getProject() {
							return PullRequestDetailPage.this.getProject();
						}

						@Override
						protected Serializable getBean() {
							return LabelsBean.of(getPullRequest());
						}

						@Override
						protected void onComponentTag(ComponentTag tag) {
							super.onComponentTag(tag);
							tag.setName("a");
						}

					});
				} else {
					labelsContainer.add(new WebMarkupContainer("head"));
				}
				labelsContainer.add(new EntityLabelsPanel<PullRequestLabel>("body", requestModel));
				labelsContainer.add(new WebMarkupContainer("labelsHelp")
						.setVisible(SecurityUtils.canModifyPullRequest(getPullRequest())));
				fragment.add(labelsContainer);

				fragment.add(new EntityReferencePanel("reference") {

					@Override
					protected EntityReference getReference() {
						return getPullRequest().getReference();
					}

				});

				fragment.add(new EntityWatchesPanel("watches") {

					@Override
					protected void onSaveWatch(EntityWatch watch) {
						OneDev.getInstance(PullRequestWatchManager.class).createOrUpdate((PullRequestWatch) watch);
					}

					@Override
					protected void onDeleteWatch(EntityWatch watch) {
						OneDev.getInstance(PullRequestWatchManager.class).delete((PullRequestWatch) watch);
					}

					@Override
					protected AbstractEntity getEntity() {
						return getPullRequest();
					}

					@Override
					protected boolean isAuthorized(User user) {
						return user.asSubject().isPermitted(new ProjectPermission(getProject(), new ReadCode()));
					}

				});

				WebMarkupContainer actions = new WebMarkupContainer("actions");
				fragment.add(actions);
				if (SecurityUtils.canModifyPullRequest(getPullRequest())) {
					actions.add(new AjaxLink<Void>("synchronize") {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(getPullRequest().isOpen());
						}

						@Override
						public void onClick(AjaxRequestTarget target) {
							getPullRequestManager().checkAsync(getPullRequest(), false, true);
							Session.get().success("Pull request synchronization submitted");
						}

					});
					actions.add(new Link<Void>("delete") {

						@Override
						public void onClick() {
							PullRequest request = getPullRequest();
							getPullRequestManager().delete(request);
							Session.get().success("Pull request #" + request.getNumber() + " deleted");

							String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(PullRequest.class);
							if (redirectUrlAfterDelete != null)
								throw new RedirectToUrlException(redirectUrlAfterDelete);
							else
								setResponsePage(ProjectPullRequestsPage.class, ProjectPullRequestsPage.paramsOf(getProject()));
						}

					}.add(new ConfirmClickModifier("Do you really want to delete this pull request?")));
				} else {
					actions.add(new WebMarkupContainer("synchronize"));
					actions.add(new WebMarkupContainer("delete"));
					actions.setVisible(false);
					fragment.add(actions);
				}

				fragment.add(new ChangeObserver() {

					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
					}

				});

				fragment.setOutputMarkupId(true);
				return fragment;
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<PullRequest>(componentId) {

					@Override
					protected EntityQuery<PullRequest> parse(String queryString, Project project) {
						return PullRequestQuery.parse(project, queryString, true);
					}

					@Override
					protected PullRequest getEntity() {
						return getPullRequest();
					}

					@Override
					protected List<PullRequest> query(EntityQuery<PullRequest> query,
							int offset, int count, ProjectScope projectScope) {
						return getPullRequestManager().query(projectScope!=null?projectScope.getProject():null, query, false, offset, count);
					}

					@Override
					protected CursorSupport<PullRequest> getCursorSupport() {
						return new CursorSupport<PullRequest>() {

							@Override
							public Cursor getCursor() {
								return WebSession.get().getPullRequestCursor();
							}

							@Override
							public void navTo(AjaxRequestTarget target, PullRequest entity, Cursor cursor) {
								WebSession.get().setPullRequestCursor(cursor);
								setResponsePage(getPageClass(), paramsOf(entity));
							}

						};
					}

				};
			}

		};
	}

	private WebMarkupContainer newMergeStrategyContainer() {
		WebMarkupContainer mergeStrategyContainer = new WebMarkupContainer("mergeStrategy");
		mergeStrategyContainer.setOutputMarkupId(true);

		mergeStrategy = getPullRequest().getMergeStrategy();

		IModel<MergeStrategy> mergeStrategyModel = new PropertyModel<MergeStrategy>(this, "mergeStrategy");

		List<MergeStrategy> mergeStrategies = Arrays.asList(MergeStrategy.values());
		DropDownChoice<MergeStrategy> editor =
				new DropDownChoice<MergeStrategy>("editor", mergeStrategyModel, mergeStrategies) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest()));
			}

		};
		editor.add(new OnChangeAjaxBehavior() {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				OneDev.getInstance(PullRequestChangeManager.class).changeMergeStrategy(getPullRequest(), mergeStrategy);
				notifyPullRequestChange(target);
			}

		});
		mergeStrategyContainer.add(editor);

		mergeStrategyContainer.add(new Label("viewer", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().toString();
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(!getPullRequest().isOpen() || !SecurityUtils.canModifyPullRequest(getPullRequest()));
			}

		});

		mergeStrategyContainer.add(new Label("help", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeStrategy().getDescription();
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				setVisible(getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest()));
			}

		});

		return mergeStrategyContainer;
	}

	private WebMarkupContainer newAutoMergeContainer() {
		WebMarkupContainer autoMergeContainer = new WebMarkupContainer("autoMerge") {
			@Override
			protected void onBeforeRender() {
				Fragment tipsContainer;
				var request = getPullRequest();
				var autoMerge = request.getAutoMerge();
				if (autoMerge.isEnabled()) {
					tipsContainer = new Fragment("tips", "autoMergeEnabledFrag", PullRequestDetailPage.this);
					WebMarkupContainer link;
					if (request.isMergeCommitMessageRequired()) {
						if (SecurityUtils.canManagePullRequests(getProject())
								|| autoMerge.getUser() != null && autoMerge.getUser().equals(SecurityUtils.getUser())) {
							link = new AjaxLink<Void>("commitMessage") {

								@Override
								public void onClick(AjaxRequestTarget target) {
									var bean = new CommitMessageBean();
									bean.setCommitMessage(autoMerge.getCommitMessage());
									new BeanEditModalPanel<>(target, bean, "Preset Commit Message") {
										@Override
										protected String getCssClass() {
											return "modal-lg commit-message no-autosize";
										}

										@Override
										protected String onSave(AjaxRequestTarget target, CommitMessageBean bean) {
											var request = getPullRequest();
											var branchProtection = getProject().getBranchProtection(request.getTargetBranch(), autoMerge.getUser());
											var errorMessage = branchProtection.checkCommitMessage(bean.getCommitMessage(),
													request.getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
											if (errorMessage != null) {
												return errorMessage;
											} else {
												autoMerge.setCommitMessage(bean.getCommitMessage());
												getPullRequestChangeManager().changeAutoMerge(request, autoMerge);
												Session.get().success("Preset commit message updated");
												close();
												getPullRequestManager().checkAutoMerge(request);
												return null;
											}
										}
									};
								}

							};
						} else {
							link = new DropdownLink("commitMessage") {

								@Override
								protected Component newContent(String id, FloatingPanel dropdown) {
									return new MultilineLabel(id, autoMerge.getCommitMessage())
											.add(AttributeAppender.append("class", "p-3"));
								}

							};
						}
					} else {
						link = new WebMarkupContainer("commitMessage");
						link.setVisible(false);
					}
					tipsContainer.add(link);
				} else {
					tipsContainer = new Fragment("tips", "autoMergeDisabledFrag", PullRequestDetailPage.this);
				}
				addOrReplace(tipsContainer);

				super.onBeforeRender();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				var request = getPullRequest();
				setVisible(request.isOpen() && request.checkMerge() != null);
			}

		};

		var autoMergeEnabled = new AtomicBoolean();
		var toggleCheck = new CheckBox("toggle", new IModel<>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return getPullRequest().getAutoMerge().isEnabled();
			}

			@Override
			public void setObject(Boolean object) {
				autoMergeEnabled.set(object);
			}

		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				if (getPullRequest().getAutoMerge().isEnabled())
					setVisible(SecurityUtils.canModifyPullRequest(getPullRequest()));
				else
					setVisible(SecurityUtils.canWriteCode(getProject()));
			}

		};
		toggleCheck.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				var request = getPullRequest();
				if (autoMergeEnabled.get()) {
					if (request.isMergeCommitMessageRequired()) {
						var bean = new CommitMessageBean();
						bean.setCommitMessage(request.getAutoMerge().getCommitMessage());
						if (bean.getCommitMessage() == null)
							bean.setCommitMessage(request.getDefaultMergeCommitMessage());
						new BeanEditModalPanel<>(target, bean, "Preset Commit Message") {

							@Override
							protected String getCssClass() {
								return "modal-lg commit-message no-autosize";
							}

							@Override
							protected String onSave(AjaxRequestTarget target, CommitMessageBean bean) {
								var request = getPullRequest();
								var user = SecurityUtils.getUser();
								var branchProtection = getProject().getBranchProtection(request.getTargetBranch(), user);
								var errorMessage = branchProtection.checkCommitMessage(bean.getCommitMessage(),
										request.getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
								if (errorMessage != null) {
									return errorMessage;
								} else {
									var autoMerge = new AutoMerge();
									autoMerge.setEnabled(true);
									autoMerge.setUser(user);
									autoMerge.setCommitMessage(bean.getCommitMessage());
									getPullRequestChangeManager().changeAutoMerge(getPullRequest(), autoMerge);
									target.add(autoMergeContainer);
									close();
									return null;
								}
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								super.onCancel(target);
								target.add(autoMergeContainer);
							}

							@Override
							protected boolean isDirtyAware() {
							 return false;
							}
						};
					} else {
					 var autoMerge = new AutoMerge();
					 autoMerge.setEnabled(true);
					 autoMerge.setUser(SecurityUtils.getUser());
					 autoMerge.setCommitMessage(request.getAutoMerge().getCommitMessage());
					 getPullRequestChangeManager().changeAutoMerge(request, autoMerge);
					 target.add(autoMergeContainer);
					}
				} else {
					var autoMerge = new AutoMerge();
					autoMerge.setCommitMessage(request.getAutoMerge().getCommitMessage());
					getPullRequestChangeManager().changeAutoMerge(request, autoMerge);
					target.add(autoMergeContainer);
				}
			}

		});
		autoMergeContainer.add(toggleCheck);

		autoMergeContainer.add(new Label("badge", new AbstractReadOnlyModel<String>() {
			@Override
			public String getObject() {
				return getPullRequest().getAutoMerge().isEnabled()? "ON": "OFF";
			}

		}) {
			@Override
			protected void onConfigure() {
				super.onConfigure();
				toggleCheck.configure();
				setVisible(!toggleCheck.isVisible());
			}
		});

		autoMergeContainer.setOutputMarkupId(true);
		return autoMergeContainer;
	}

	private WebMarkupContainer newStatusBarContainer() {
		WebMarkupContainer statusBarContainer = new WebMarkupContainer("statusBar");

		statusBarContainer.add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getStatus().toString();
			}

		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();

				add(new ChangeObserver() {

					@Override
					public Collection<String> findObservables() {
						return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
					}

				});

				add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						PullRequest request = getPullRequest();
						if (request.isDiscarded())
							return " badge-info";
						else if (request.isMerged())
							return " badge-success";
						else
							return " badge-warning";
					}

				}));
				setOutputMarkupId(true);
			}

		});

		statusBarContainer.add(new DropdownLink("clone") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				Fragment fragment = new Fragment(id, "cloneFrag", PullRequestDetailPage.this);
				fragment.add(new Label("headRef", getPullRequest().getHeadRef()));
				fragment.add(new Label("mergeRef", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getPullRequest().getMergeRef();
					}

				}) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getPullRequest().checkMergePreview() != null
								&& getPullRequest().checkMergePreview().getMergeCommitHash() != null);
					}

				});
				return fragment;
			}

		});

		statusBarContainer.add(new BookmarkablePageLink<Void>(
				"newPullRequest",
				NewPullRequestPage.class,
				NewPullRequestPage.paramsOf(getProject())));

		return statusBarContainer;
	}

	private WebMarkupContainer newDescriptionContainer() {
		PullRequest request = getPullRequest();

		var descriptionContainer = new WebMarkupContainer("description");
		descriptionContainer.add(new UserIdentPanel("submitterAvatar", request.getSubmitter(), Mode.AVATAR));
		descriptionContainer.add(new Label("submitterName", request.getSubmitter().getDisplayName()));
		descriptionContainer.add(new Label("submitDate", DateUtils.formatAge(request.getSubmitDate()))
			.add(new AttributeAppender("title", DateUtils.formatDateTime(request.getSubmitDate()))));
		
		descriptionContainer.add(new CommentPanel("content") {

			@Override
			protected String getComment() {
				return getPullRequest().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				OneDev.getInstance(PullRequestChangeManager.class).changeDescription(getPullRequest(), comment);
				((BasePage)getPage()).notifyObservableChange(target,
						PullRequest.getChangeObservable(getPullRequest().getId()));
			}

			@Nullable
			@Override
			protected String getAutosaveKey() {
				return "pull-request:" + getPullRequest().getId() + ":description"; 
			}

			@Override
			protected Project getProject() {
				return getPullRequest().getTargetProject();
			}

			@Override
			protected List<User> getParticipants() {
				return getPullRequest().getParticipants();
			}
			
			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getPullRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(getProject()));
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canModifyPullRequest(getPullRequest());
			}

			@Override
			protected String getRequiredLabel() {
				return null;
			}

			@Override
			protected String getEmptyDescription() {
				return "No description";
			}
			
			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return new ContentVersionSupport() {

					@Override
					public long getVersion() {
						return 0;
					}
					
				};
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}

			@Override
			protected Collection<? extends EntityReaction> getReactions() {
				return getPullRequest().getReactions();
			}

			@Override
			protected void onToggleEmoji(AjaxRequestTarget target, String emoji) {
				OneDev.getInstance(PullRequestReactionManager.class).toggleEmoji(
						SecurityUtils.getUser(), 
						getPullRequest(), 
						emoji);
			}
			
		});

		return descriptionContainer;
	}
	
	private WebMarkupContainer newSummaryContributions() {
		return new ListView<PullRequestSummaryPart>("contributions",
				new LoadableDetachableModel<List<PullRequestSummaryPart>>() {

			@Override
			protected List<PullRequestSummaryPart> load() {
				List<PullRequestSummaryContribution> contributions =
						new ArrayList<>(OneDev.getExtensions(PullRequestSummaryContribution.class));
				contributions.sort(Comparator.comparing(PullRequestSummaryContribution::getOrder));

				List<PullRequestSummaryPart> parts = new ArrayList<>();
				for (PullRequestSummaryContribution contribution: contributions)
					parts.addAll(contribution.getParts(getPullRequest()));

				return parts;
			}

		}) {

			@Override
			protected void populateItem(ListItem<PullRequestSummaryPart> item) {
				PullRequestSummaryPart part = item.getModelObject();
				item.add(new Label("head", part.getReportName()));
				item.add(part.render("body"));
			}

		};
	}

	private WebMarkupContainer newOperationsContainer() {
		WebMarkupContainer operationsContainer = new WebMarkupContainer("requestOperations");
		operationsContainer.add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(PullRequest.getChangeObservable(getPullRequest().getId()));
			}

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				super.onObservableChanged(handler, changedObservables);
				handler.appendJavaScript("setTimeout(function() {$(window).resize();}, 0);");
			}

		});

		operationsContainer.setOutputMarkupPlaceholderTag(true);
		operationsContainer.setVisible(SecurityUtils.getAuthUser() != null);

		operationsContainer.add(new MenuLink("updateSourceBranch") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				var menuItems = new ArrayList<MenuItem>();
				menuItems.add(new MenuItem() {
					@Override
					public String getLabel() {
						return "Merge";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								var request = getPullRequest();
								var targetHead = request.getTarget().getObjectId();
								var sourceHead = request.getSourceHead();
								var result = checkUpdateSourceBranch(true);
								if (result instanceof ObjectId) {
									var mergeCommit = getProject().getRevCommit((ObjectId) result, true);
									var bean = new CommitMessageBean();
									bean.setCommitMessage(mergeCommit.getFullMessage());
									new BeanEditModalPanel<>(target, bean, "Merge Target Branch into Source Branch") {
										@Override
										protected String getCssClass() {
											return "modal-lg commit-message no-autosize";
										}

										@Override
										protected boolean isDirtyAware() {
											return false;
										}

										@Override
										protected String onSave(AjaxRequestTarget target, CommitMessageBean bean) {
											var request = getPullRequest();
											var user = SecurityUtils.getAuthUser();
											var protection = request.getSourceProject().getBranchProtection(request.getSourceBranch(), user);
											var errorMessage = protection.checkCommitMessage(bean.getCommitMessage(), true);
											if (errorMessage != null)
												return errorMessage;
											if (targetHead.equals(request.getTarget().getObjectId())
													&& sourceHead.equals(request.getSourceHead())) {
												var gitService = OneDev.getInstance(GitService.class);
												var amendedCommitId = gitService.amendCommit(request.getProject(), mergeCommit.copy(),
														mergeCommit.getAuthorIdent(), mergeCommit.getCommitterIdent(),
														bean.getCommitMessage());
												updateSourceBranch(amendedCommitId);
												getSession().success("Source branch updated successfully");
											} else {
												getSession().warn("Target or source branch is updated. Please try again");
											}
											close();
											return null;
										}

									};
								} else {
									getSession().error((String)result);
								}
							}
						};
					}
				});
				menuItems.add(new MenuItem() {
					@Override
					public String getLabel() {
						return "Rebase";
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								var request = getPullRequest();
								var targetHead = request.getTarget().getObjectId();
								var sourceHead = request.getSourceHead();
								var result = checkUpdateSourceBranch(false);
								if (result instanceof ObjectId) {
									new ConfirmModalPanel(target) {

										@Override
										protected void onConfirm(AjaxRequestTarget target) {
											var request = getPullRequest();
											if (targetHead.equals(request.getTarget().getObjectId())
													&& sourceHead.equals(request.getSourceHead())) {
												updateSourceBranch((ObjectId)result);
												getSession().success("Source branch updated successfully");
											} else {
												getSession().warn("Target or source branch is updated. Please try again");
											}
										}

										@Override
										protected String getConfirmInput() {
											return null;
										}

										@Override
										protected String getConfirmMessage() {
											return "You are rebasing source branch on top of target branch";
										}
									};
								} else {
									getSession().error((String)result);
								}
							}
						};
					}
				});
				return menuItems;
			}

			private boolean canOperate() {
				return SecurityUtils.canWriteCode(getPullRequest().getProject())
						&& getPullRequest().isOpen()
						&& getPullRequest().getSourceHead() != null
						&& getPullRequest().isSourceOutdated();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}
		});

		operationsContainer.add(new ModalLink("approve") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					PullRequestReview review = request.getReview(SecurityUtils.getAuthUser());
					return review != null && review.getStatus() == Status.PENDING;
				} else {
					return false;
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestReviewManager().review(getPullRequest(), true, getComment());
							notifyPullRequestChange(target);
							Session.get().success("Approved");
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Approve";
					}
				};
			}

		});

		operationsContainer.add(new ModalLink("requestForChanges") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				if (request.isOpen()) {
					PullRequestReview review = request.getReview(SecurityUtils.getAuthUser());
					return review != null && review.getStatus() == Status.PENDING;
				} else {
					return false;
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestReviewManager().review(getPullRequest(), false, getComment());
							notifyPullRequestChange(target);
							Session.get().success("Requested For changes");
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Request For Changes";
					}
				};
			}

		});

		operationsContainer.add(new ModalLink("merge") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				return SecurityUtils.canWriteCode(request.getProject()) && request.checkMerge() == null;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new MergePullRequestOptionPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							var request = getPullRequest();
							var branchProtection = getProject().getBranchProtection(request.getTargetBranch(), request.getSubmitter());
							var commitMessage = getCommitMessage();
							if (commitMessage != null) {
								var errorMessage = branchProtection.checkCommitMessage(commitMessage,
										request.getMergeStrategy() != SQUASH_SOURCE_BRANCH_COMMITS);
								if (errorMessage != null)
									return errorMessage;
							}
							getPullRequestManager().merge(SecurityUtils.getUser(), getPullRequest(), commitMessage);
							notifyPullRequestChange(target);
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

				};
			}

		});

		operationsContainer.add(new ModalLink("discard") {

			private boolean canOperate() {
				return getPullRequest().isOpen() && SecurityUtils.canModifyPullRequest(getPullRequest());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestManager().discard(getPullRequest(), getComment());
							notifyPullRequestChange(target);
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Discard";
					}
				};
			}

		});

		operationsContainer.add(new ModalLink("reopen") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				return SecurityUtils.canModifyPullRequest(request) && request.checkReopen() == null;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestManager().reopen(getPullRequest(), getComment());
							notifyPullRequestChange(target);
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Reopen";
					}
				};
			}

		});

		operationsContainer.add(new ModalLink("deleteSourceBranch") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				return request.checkDeleteSourceBranch() == null
						&& SecurityUtils.canModifyPullRequest(request)
						&& SecurityUtils.canDeleteBranch(request.getSourceProject(), request.getSourceBranch());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestManager().deleteSourceBranch(getPullRequest(), getComment());
							notifyPullRequestChange(target);
							Session.get().success("Deleted source branch");
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Delete Source Branch";
					}
				};
			}

		});

		operationsContainer.add(new ModalLink("restoreSourceBranch") {

			private boolean canOperate() {
				PullRequest request = getPullRequest();
				return request.checkRestoreSourceBranch() == null
						&& SecurityUtils.canModifyPullRequest(request)
						&& SecurityUtils.canWriteCode(request.getSourceProject());
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(canOperate());
			}

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new OperationCommentPanel(id, modal, latestUpdateId) {

					@Override
					protected String operate(AjaxRequestTarget target) {
						if (canOperate()) {
							getPullRequestManager().restoreSourceBranch(getPullRequest(), getComment());
							notifyPullRequestChange(target);
							Session.get().success("Restored source branch");
							return null;
						} else {
							return "Can not perform this operation now";
						}
					}

					@Override
					protected String getTitle() {
						return "Confirm Restore Source Branch";
					}
				};
			}

		});

		return operationsContainer;
	}

	/**
	 * @return String type representing error message if check failed; otherwise ObjectId type
	 * representing merge commit id
	 */
	private Object checkUpdateSourceBranch(boolean updateByMerge) {
		var request = getPullRequest();
		var user = SecurityUtils.getAuthUser();
		var project = request.getProject();
		var protection = request.getSourceProject().getBranchProtection(request.getSourceBranch(), user);

		ObjectId sourceHead = request.getSourceHead();
		ObjectId targetHead = request.getTarget().getObjectId();
		ObjectId mergeCommitId;

		var gitService = getGitService();
		if (updateByMerge) {
			var commitMessage = "Merge branch '" + request.getTargetBranch() +"'";
			if (!request.getSourceProject().equals(project))
				commitMessage += " of project '" + project.getPath() + "' ";
			commitMessage += " into branch '" + request.getSourceBranch() + "'";
			mergeCommitId = gitService.merge(project, targetHead, sourceHead,
					false, user.asPerson(), user.asPerson(), commitMessage, false);
		} else {
			mergeCommitId = gitService.rebase(project, sourceHead, targetHead, user.asPerson());
		}
		if (mergeCommitId == null)
			return "There are merge conflicts";
		if (protection.isReviewRequiredForPush(project, sourceHead, mergeCommitId, new HashMap<>()))
			return "Review required for this change. Submit pull request instead";
		var buildRequirement = protection.getBuildRequirement(project, sourceHead, mergeCommitId, new HashMap<>());
		if (!buildRequirement.getRequiredJobs().isEmpty())
			return "This change needs to be verified by some jobs. Submit pull request instead";
		if (protection.isCommitSignatureRequired()
				&& !project.hasValidCommitSignature(project.getRevCommit(targetHead, true))) {
			return "No valid signature for head commit of target branch";
		}
		if (protection.isCommitSignatureRequired()
				&& OneDev.getInstance(SettingManager.class).getGpgSetting().getSigningKey() == null) {
			return "Commit signature required but no GPG signing key specified";
		}
		var error = gitService.checkCommitMessages(protection, project, sourceHead, mergeCommitId, new HashMap<>());
		if (error != null)
			return "Error validating commit message of '" + error.getCommitId().name() + "': " + error.getErrorMessage();

		return mergeCommitId;
	}

	private void updateSourceBranch(ObjectId commitId) {
		var request = getPullRequest();
		var gitService = getGitService();
		if (!request.getSourceProject().equals(request.getTargetProject()))
			gitService.fetch(request.getSourceProject(), request.getTargetProject(), commitId.name());
		var oldCommitId = request.getSourceHead();
		getGitService().updateRef(request.getSourceProject(), request.getSourceRef(), commitId, oldCommitId);
	}

	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(PullRequest pullRequest) {
		return paramsOf(pullRequest.getProject(), pullRequest.getNumber());
	}

	public static PageParameters paramsOf(Project project, Long pullRequestNumber) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_REQUEST, pullRequestNumber);
		return params;
	}

	@Override
	public PullRequest getPullRequest() {
		return requestModel.getObject();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new PullRequestDetailResourceReference()));
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canReadCode(getProject());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("pullRequests", ProjectPullRequestsPage.class,
				ProjectPullRequestsPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("pullRequestNumber", getPullRequest().getReference().toString(getProject())));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		return getPullRequest().getTitle() + " (" + getPullRequest().getReference().toString(getProject()) + ")";
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement() && SecurityUtils.canReadCode(project))
			return new ViewStateAwarePageLink<Void>(componentId, ProjectPullRequestsPage.class, ProjectPullRequestsPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}

	private void notifyPullRequestChange(AjaxRequestTarget target) {
		((BasePage)getPage()).notifyObservableChange(target,
				PullRequest.getChangeObservable(getPullRequest().getId()));
	}

}

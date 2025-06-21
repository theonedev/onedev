package io.onedev.server.web.page.project.builds.detail;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspecmodel.inputspec.InputContext;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.job.JobAuthorizationContext;
import io.onedev.server.job.JobAuthorizationContextAware;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.pack.PackSupport;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.build.side.BuildSidePanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.comment.CommentPanel;
import io.onedev.server.web.component.comment.ReactionSupport;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.joblist.JobListPanel;
import io.onedev.server.web.component.link.BuildSpecLink;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.modal.message.MessageModal;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.changes.BuildChangesPage;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.page.project.builds.detail.pack.BuildPacksPage;
import io.onedev.server.web.page.project.builds.detail.pipeline.BuildPipelinePage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.web.util.DeleteCallback;

public abstract class BuildDetailPage extends ProjectPage 
		implements InputContext, BuildAware, JobAuthorizationContextAware {

	public static final String PARAM_BUILD = "build";
	
	protected final IModel<Build> buildModel;
	
	private final IModel<List<Job>> promotionsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			List<Job> promoteJobs = new ArrayList<>();
			if (getBuild().getSpec() != null) {
				for (Job job: getBuild().getSpec().getJobMap().values()) {
					for (JobDependency dependency: job.getJobDependencies()) {
						/*
						if (dependency.getJobName().equals(getBuild().getJobName()) 
								&& getBuild().matchParams(dependency.getJobParams())) { 
							promoteJobs.add(job);
						}
						*/
						// Do not use logic above due to issue #1219
						if (dependency.getJobName().equals(getBuild().getJobName())) 
							promoteJobs.add(job);
					}
				}
			}
			return promoteJobs;
		}
		
	};
	
	public BuildDetailPage(PageParameters params) {
		super(params);
		
		String buildNumberString = params.get(PARAM_BUILD).toString();
		if (StringUtils.isBlank(buildNumberString))
			throw new RestartResponseException(ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject(), null, 0));
			
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				Long buildNumber = params.get(PARAM_BUILD).toLong();
				Build build = OneDev.getInstance(BuildManager.class).find(getProject(), buildNumber);
				if (build == null)
					throw new EntityNotFoundException(MessageFormat.format(_T("Unable to find build #{0} in project {1}"), buildNumber, getProject()));
				else if (!build.getProject().equals(getProject()))
					throw new RestartResponseException(getPageClass(), paramsOf(build));
				else
					return build;
			}

		};
	
		if (!getBuild().isValid())
			throw new RestartResponseException(InvalidBuildPage.class, InvalidBuildPage.paramsOf(getBuild()));
	}
	
	@Override
	public Build getBuild() {
		return buildModel.getObject();
	}
	
	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccessBuild(getBuild());
	}
	
	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (project.isCodeManagement()) 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(project, 0));
		else
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}

	private ChangeObserver newBuildObserver(Long buildId) {
		return new ChangeObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				super.onObservableChanged(handler, changedObservables);
				handler.appendJavaScript("$(window).resize();");
			}
			
			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(Build.getDetailChangeObservable(buildId));
			}
			
		};
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("summary", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getBuild().getSummary(getProject());
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(newBuildObserver(getBuild().getId()));
				setOutputMarkupId(true);
			}
			
		});
		
		WebMarkupContainer statusContainer = new WebMarkupContainer("status");
		add(statusContainer);
		statusContainer.add(newBuildObserver(getBuild().getId()));
		statusContainer.setOutputMarkupId(true);
		statusContainer.add(new BuildStatusIcon("statusIcon", new AbstractReadOnlyModel<Status>() {

			@Override
			public Status getObject() {
				return getBuild().getStatus();
			}
			
		}) {
			
			@Override
			protected Collection<String> getChangeObservables() {
				return Sets.newHashSet(Build.getDetailChangeObservable(getBuild().getId()));
			}
			
		});
		statusContainer.add(new Label("statusLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T(buildModel.getObject().getStatus().toString());
			}
			
		}));
		
		add(new WebMarkupContainer("actions") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new AjaxLink<Void>("rebuild") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to rebuild?")));
					}

					private void resubmit(Serializable paramBean) {
						OneDev.getInstance(JobManager.class).resubmit(getBuild(), _T("Resubmitted manually"));
						setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(getBuild()));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						resubmit(getBuild().getParamBean());
						target.focusComponent(null);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(getBuild().isFinished() && getBuild().getJob() != null
								&& SecurityUtils.canRunJob(getProject(), getBuild().getJobName()));
					}

				}.setOutputMarkupId(true));

				add(new AjaxLink<Void>("cancel") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to cancel this build?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						OneDev.getInstance(JobManager.class).cancel(getBuild());
						getSession().success(_T("Cancel request submitted"));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!getBuild().isFinished() && SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName()));
					}

				}.setOutputMarkupId(true));

				add(new AjaxLink<Void>("terminal") {

					private TerminalManager getTerminalManager() {
						return OneDev.getInstance(TerminalManager.class);
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (isSubscriptionActive()) {
							target.appendJavaScript(String.format("onedev.server.buildDetail.openTerminal('%s');",
									getTerminalManager().getTerminalUrl(getBuild())));
						} else {
							new MessageModal(target) {

								@Override
								protected Component newMessageContent(String componentId) {
									return new Label(componentId, _T("Interactive web shell access to running jobs is an enterprise feature. <a href='https://onedev.io/pricing' target='_blank'>Try free</a> for 30 days")).setEscapeModelStrings(false);
								}
							};
						}
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						JobManager jobManager = OneDev.getInstance(JobManager.class);
						JobContext jobContext = jobManager.getJobContext(getBuild().getId());
						setVisible(jobContext!= null && SecurityUtils.canOpenTerminal(getBuild()));
					}

				}.setOutputMarkupId(true));

				add(new DropdownLink("promotions") {

					@Override
					protected Component newContent(String id, FloatingPanel dropdown) {
						return new JobListPanel(id, getBuild().getCommitId(),
								getBuild().getRefName(), promotionsModel.getObject()) {

							@Override
							protected Project getProject() {
								return BuildDetailPage.this.getProject();
							}

							@Override
							protected void onRunJob(AjaxRequestTarget target) {
								dropdown.close();
							}

							@Override
							protected PullRequest getPullRequest() {
								return getBuild().getRequest();
							}

						};
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(!promotionsModel.getObject().isEmpty());
					}

				});
				add(newBuildObserver(getBuild().getId()));
			}
		});
		
		add(new SideInfoLink("moreInfo"));
		
		add(new WebMarkupContainer("buildSpecNotFound") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getSpec() == null);
			}
			
		});
		
		WebMarkupContainer jobNotFoundContainer = new WebMarkupContainer("jobNotFound") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getSpec() != null && getBuild().getJob() == null);
			}
			
		};
		
		jobNotFoundContainer.add(new Label("message", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return MessageFormat.format(_T("Job \"{0}\" associated with the build not found."), getBuild().getJobName());
			}
		}));
		jobNotFoundContainer.add(new BuildSpecLink("buildSpec", getBuild().getCommitId()) {

			@Override
			protected Project getProject() {
				return getBuild().getProject();
			}

			@Override
			protected PullRequest getPullRequest() {
				return getBuild().getRequest();
			}
			
		});
		
		add(jobNotFoundContainer);
		
		add(new CommentPanel("description") {
			
			@Override
			protected String getComment() {
				return getBuild().getDescription();
			}

			@Override
			protected void onSaveComment(AjaxRequestTarget target, String comment) {
				getBuild().setDescription(comment);
				OneDev.getInstance(BuildManager.class).update(getBuild());
			}
			
			@Override
			protected Project getProject() {
				return getBuild().getProject();
			}

			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return null;
			}

			@Override
			protected boolean canManageComment() {
				return SecurityUtils.canManageBuild(getBuild());
			}

			@Override
			protected String getRequiredLabel() {
				return null;
			}

			@Override
			protected String getEmptyDescription() {
				return _T("No description");
			}

			@Override
			protected ContentVersionSupport getContentVersionSupport() {
				return null;
			}

			@Override
			protected DeleteCallback getDeleteCallback() {
				return null;
			}

			@Override
			protected String getAutosaveKey() {
				return "build:" + getBuild().getId() + ":description";
			}

			@Override
			protected ReactionSupport getReactionSupport() {
				return null;
			}

			@Override
			protected boolean isQuoteEnabled() {
				return false;
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().getDescription() != null || SecurityUtils.canManageBuild(getBuild()));
			}
		});

		add(new Tabbable("buildTabs", new LoadableDetachableModel<>() {

			@Override
			protected List<Tab> load() {
				List<Tab> tabs = new ArrayList<>();

				if (SecurityUtils.canAccessLog(getBuild())) {
					tabs.add(new BuildTab(Model.of(_T("Log")), BuildLogPage.class, BuildLogPage.paramsOf(getBuild())) {

						@Override
						protected Component renderOptions(String componentId) {
							BuildLogPage page = (BuildLogPage) getPage();
							return page.renderOptions(componentId);
						}

					});
				}

				if (SecurityUtils.canAccessPipeline(getBuild())) 
					tabs.add(new BuildTab(Model.of(_T("Pipeline")), BuildPipelinePage.class, BuildPipelinePage.paramsOf(getBuild())));
				
				if (SecurityUtils.canManageBuild(getBuild()) || getBuild().getRootArtifacts().size() != 0) {
					tabs.add(new BuildTab(Model.of(_T("Artifacts")), BuildArtifactsPage.class, BuildArtifactsPage.paramsOf(getBuild())));
				}

				var packSupports = new ArrayList<>(OneDev.getExtensions(PackSupport.class));
				packSupports.sort(Comparator.comparing(PackSupport::getOrder));
				for (var packSupport: packSupports) {
					var packType = packSupport.getPackType();
					if (getBuild().getPacks().stream().anyMatch(it -> it.getType().equals(packType) && SecurityUtils.canReadPack(it.getProject()))) {
						tabs.add(new BuildTab(Model.of(packSupport.getPackType()), Model.of(packSupport.getPackIcon()), BuildPacksPage.class, BuildPacksPage.paramsOf(getBuild(), packType)) {
							@Override
							public boolean isActive(Page currentPage) {
								if (super.isActive(currentPage)) {
									if (currentPage instanceof BuildPacksPage) {
										var buildPacksPage = (BuildPacksPage) currentPage;
										return buildPacksPage.getPackType().equals(packSupport.getPackType());
									} else {
										return true;
									}
								} else {
									return false;
								}
							}
						});
					}
				}
				
				var params = FixedIssuesPage.paramsOf(getBuild(), getProject().getHierarchyDefaultFixedIssueQuery(getBuild().getJobName()));
				tabs.add(new BuildTab(Model.of(_T("Fixed Issues")), FixedIssuesPage.class, params));

				if (SecurityUtils.canReadCode(getProject()))
					tabs.add(new BuildTab(Model.of(_T("Code Changes")), BuildChangesPage.class, BuildChangesPage.paramsOf(getBuild())));

				List<BuildTabContribution> contributions = new ArrayList<>(OneDev.getExtensions(BuildTabContribution.class));
				contributions.sort(Comparator.comparing(BuildTabContribution::getOrder));

				for (BuildTabContribution contribution : contributions)
					tabs.addAll(contribution.getTabs(getBuild()));

				return tabs;
			}

		}) {

			@Override
			public void onInitialize() {
				super.onInitialize();
				add(newBuildObserver(getBuild().getId()));
				setOutputMarkupId(true);
			}
			
		});
		
		add(new SideInfoPanel("side") {

			@Override
			protected Component newBody(String componentId) {
				return new BuildSidePanel(componentId) {

					@Override
					protected Build getBuild() {
						return BuildDetailPage.this.getBuild();
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						return new Link<Void>(componentId) {

							@Override
							public void onClick() {
								OneDev.getInstance(BuildManager.class).delete(getBuild());
								var oldAuditContent = VersionedXmlDoc.fromBean(getBuild()).toXML();
								getAuditManager().audit(getBuild().getProject(), "deleted build \"" + getBuild().getReference().toString(getBuild().getProject()) + "\"", oldAuditContent, null);
								
								Session.get().success(MessageFormat.format(_T("Build #{0} deleted"), getBuild().getNumber()));
								
								String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Build.class);
								if (redirectUrlAfterDelete != null)
									throw new RedirectToUrlException(redirectUrlAfterDelete);
								else
									setResponsePage(ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject()));
							}
							
						}.add(new ConfirmClickModifier(_T("Do you really want to delete this build?")));
					}
					
				};
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<Build>(componentId) {

					@Override
					protected EntityQuery<Build> parse(String queryString, Project project) {
						return BuildQuery.parse(project, queryString, true, true);
					}

					@Override
					protected Build getEntity() {
						return getBuild();
					}

					@Override
					protected List<Build> query(EntityQuery<Build> query, int offset, int count, ProjectScope projectScope) {
						BuildManager buildManager = OneDev.getInstance(BuildManager.class);
						return buildManager.query(projectScope!=null?projectScope.getProject():null, query, false, offset, count);
					}

					@Override
					protected CursorSupport<Build> getCursorSupport() {
						return new CursorSupport<Build>() {

							@Override
							public Cursor getCursor() {
								return WebSession.get().getBuildCursor();
							}

							@Override
							public void navTo(AjaxRequestTarget target, Build entity, Cursor cursor) {
								WebSession.get().setBuildCursor(cursor);
								setResponsePage(getPageClass(), getPageParameters().mergeWith(paramsOf(entity)));
							}
							
						};
					}
					
				};				
			}
			
		});
		
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new BuildDetailResourceReference()));
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Build build) {
		return paramsOf(build.getProject(), build.getNumber());
	}
	
	public static PageParameters paramsOf(Project project, Long buildNumber) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_BUILD, buildNumber);
		return params;
	}
	
	@Override
	public List<String> getInputNames() {
		if (getBuild().getJob() != null)
			return new ArrayList<>(getBuild().getJob().getParamSpecMap().keySet());
		else
			return new ArrayList<>();
	}

	@Override
	public ParamSpec getInputSpec(String paramName) {
		if (getBuild().getJob() != null)
			return getBuild().getJob().getParamSpecMap().get(paramName);
		else
			return null;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("builds", ProjectBuildsPage.class, 
				ProjectBuildsPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("buildNumber", getBuild().getReference().toString(getProject())));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		var pageTitle = getBuild().getReference().toString(getProject());
		if (getBuild().getVersion() != null)
			pageTitle += " (" + getBuild().getVersion() + ")";
		return pageTitle;
	}

	@Override
	public JobAuthorizationContext getJobAuthorizationContext() {
		return new JobAuthorizationContext(getProject(), getBuild().getCommitId(), 
				getBuild().getSubmitter(), getBuild().getRequest());
	}
	
}

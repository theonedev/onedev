package io.onedev.server.web.page.project.builds.detail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.build.ProjectBuildSetting;
import io.onedev.server.model.support.inputspec.InputContext;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.build.side.BuildSidePanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.job.joblist.JobListPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModalPanel;
import io.onedev.server.web.component.sideinfo.SideInfoLink;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.PageTabHead;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Password;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.changes.BuildChangesPage;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;

@SuppressWarnings("serial")
public abstract class BuildDetailPage extends ProjectPage 
		implements InputContext, ScriptIdentityAware, BuildAware {

	public static final String PARAM_BUILD = "build";
	
	protected final IModel<Build> buildModel;
	
	private final IModel<List<Job>> downstreamJobsModel = new LoadableDetachableModel<List<Job>>() {

		@Override
		protected List<Job> load() {
			List<Job> downstreamJobs = new ArrayList<>();
			BuildSpec buildSpec = getProject().getBuildSpec(getBuild().getCommitId());
			for (Job job: buildSpec.getJobs()) {
				for (JobDependency dependency: job.getJobDependencies()) {
					if (dependency.getJobName().equals(getBuild().getJobName()) 
							&& getBuild().matchParams(dependency.getJobParams())) { 
						downstreamJobs.add(job);
					}
				}
			}
			return downstreamJobs;
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
					throw new EntityNotFoundException("Unable to find build #" + buildNumber + " in project " + getProject());
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
		return SecurityUtils.canAccess(getBuild());
	}
	
	private WebSocketObserver newBuildObserver(Long buildId) {
		return new WebSocketObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler) {
				handler.add(component);
				handler.appendJavaScript("$(window).resize();");
			}
			
			@Override
			public Collection<String> getObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(buildId));
			}
			
		};
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuilder builder = new StringBuilder("#" + getBuild().getNumber());
				if (getBuild().getVersion() != null)
					builder.append(" (" + getBuild().getVersion() + ")");
				return builder.toString();
				
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
			protected Collection<String> getWebSocketObservables() {
				return Sets.newHashSet(Build.getWebSocketObservable(getBuild().getId()));
			}
			
		});
		statusContainer.add(new Label("statusLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return buildModel.getObject().getStatus().getDisplayName();
			}
			
		}));
		
		WebMarkupContainer actionsContainer = new WebMarkupContainer("actions");
		actionsContainer.setOutputMarkupId(true);
		add(actionsContainer);
		
		actionsContainer.add(newBuildObserver(getBuild().getId()));
		
		actionsContainer.add(new AjaxLink<Void>("rebuild") {

			private void resubmit(Serializable paramBean) {
				Map<String, List<String>> paramMap = ParamSupply.getParamMap(getBuild().getJob(), paramBean, 
						getBuild().getJob().getParamSpecMap().keySet());
				OneDev.getInstance(JobManager.class).resubmit(getBuild(), paramMap, "Resubmitted manually");
				setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(getBuild()));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				Build build = getBuild();

				Serializable paramBean = build.getParamBean();

				Collection<String> secretParamNames = new ArrayList<>();
				BeanDescriptor descriptor = new BeanDescriptor(paramBean.getClass());
				for (List<PropertyDescriptor> groupProperties: descriptor.getProperties().values()) {
					for (PropertyDescriptor property: groupProperties) {
						if (property.getPropertyGetter().getAnnotation(Password.class) != null 
								&& build.isParamVisible(property.getDisplayName())) {
							secretParamNames.add(property.getPropertyName());
						}
					}
				}
				
				if (!secretParamNames.isEmpty()) {
					new BeanEditModalPanel(target, paramBean, secretParamNames, false, "Rebuild #" + build.getNumber()) {
						
						@Override
						protected void onSave(AjaxRequestTarget target, Serializable bean) {
							resubmit(paramBean);
						}
						
					};
				} else {
					new ConfirmModalPanel(target) {
						
						@Override
						protected void onConfirm(AjaxRequestTarget target) {
							resubmit(paramBean);
						}
						
						@Override
						protected String getConfirmMessage() {
							return "Do you really want to rerun this build?";
						}
						
						@Override
						protected String getConfirmInput() {
							return null;
						}
						
					};
				}
				target.focusComponent(null);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().isFinished() && SecurityUtils.canRunJob(getProject(), getBuild().getJobName()));
			}
			
		});
		
		actionsContainer.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				OneDev.getInstance(JobManager.class).cancel(getBuild());
				getSession().success("Cancel request submitted");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getBuild().isFinished() && SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName()));
			}
			
		}.add(new ConfirmClickModifier("Do you really want to cancel this build?")));
		
		add(new DropdownLink("downstream") {

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new JobListPanel(id, getBuild().getCommitId(),  
						getBuild().getRefName(), downstreamJobsModel.getObject()) {
					
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
				setVisible(!downstreamJobsModel.getObject().isEmpty());
			}
			
		});
		
		add(new SideInfoLink("moreInfo"));
		
		add(new Label("errorMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getBuild().getErrorMessage();
			}
			
		}) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(newBuildObserver(getBuild().getId()));
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().isFinished() && getBuild().getErrorMessage() != null);
			}
			
		});
		
		add(new Tabbable("buildTabs", new LoadableDetachableModel<List<? extends Tab>>() {

			@Override
			protected List<Tab> load() {
				List<Tab> tabs = new ArrayList<>();

				if (SecurityUtils.canAccessLog(getBuild())) {
					tabs.add(new BuildTab("Log", BuildLogPage.class) {
	
						@Override
						protected Component renderOptions(String componentId) {
							BuildLogPage page = (BuildLogPage) getPage();
							return page.renderOptions(componentId);
						}
						
					});
				}
				
				LockUtils.read(getBuild().getArtifactsLockKey(), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						if (getBuild().getArtifactsDir().exists()) 
							tabs.add(new BuildTab("Artifacts", BuildArtifactsPage.class));
						return null;
					}
					
				});
				
				tabs.add(new BuildTab("Fixed Issues", FixedIssuesPage.class) {

					@Override
					public Component render(String componentId) {
						return new PageTabHead(componentId, this) {

							@Override
							protected Link<?> newLink(String linkId, Class<? extends Page> pageClass) {
								ProjectBuildSetting buildSetting = getProject().getBuildSetting();
								return new ViewStateAwarePageLink<Void>(
										linkId, pageClass, FixedIssuesPage.paramsOf(getBuild(), 
										buildSetting.getDefaultFixedIssueQuery(getBuild().getJobName())));
							}
							
						};
					}
					
				});
				
				if (SecurityUtils.canReadCode(getProject()))
					tabs.add(new BuildTab("Changes", BuildChangesPage.class));
				
				List<BuildTabContribution> contributions = new ArrayList<>(OneDev.getExtensions(BuildTabContribution.class));
				contributions.sort(Comparator.comparing(BuildTabContribution::getOrder));
				
				for (BuildTabContribution contribution: contributions)
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
								
								Session.get().success("Build #" + getBuild().getNumber() + " deleted");
								
								String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Build.class);
								if (redirectUrlAfterDelete != null)
									throw new RedirectToUrlException(redirectUrlAfterDelete);
								else
									setResponsePage(ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject()));
							}
							
						}.add(new ConfirmClickModifier("Do you really want to delete this build?"));
					}
					
				};
			}

			@Override
			protected Component newTitle(String componentId) {
				return new EntityNavPanel<Build>(componentId) {

					@Override
					protected EntityQuery<Build> parse(String queryString, boolean inProject) {
						return BuildQuery.parse(inProject?getProject():null, queryString, true, true);
					}

					@Override
					protected Build getEntity() {
						return getBuild();
					}

					@Override
					protected List<Build> query(EntityQuery<Build> query, int offset, int count, boolean inProject) {
						BuildManager buildManager = OneDev.getInstance(BuildManager.class);
						return buildManager.query(inProject?getProject():null, query, offset, count);
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
		response.render(CssHeaderItem.forReference(new BuildDetailCssResourceReference()));
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Build build) {
		return paramsOf(build.getFQN());
	}
	
	public static PageParameters paramsOf(ProjectScopedNumber buildFQN) {
		PageParameters params = ProjectPage.paramsOf(buildFQN.getProject());
		params.add(PARAM_BUILD, buildFQN.getNumber());
		return params;
	}
	
	@Override
	public List<String> getInputNames() {
		return new ArrayList<>(getBuild().getJob().getParamSpecMap().keySet());
	}

	@Override
	public ParamSpec getInputSpec(String paramName) {
		return Preconditions.checkNotNull(getBuild().getJob().getParamSpecMap().get(paramName));
	}

	@Override
	public ScriptIdentity getScriptIdentity() {
		return new JobIdentity(getBuild().getProject(), getBuild().getCommitId());
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new BookmarkablePageLink<Void>("builds", ProjectBuildsPage.class, 
				ProjectBuildsPage.paramsOf(getProject())));
		fragment.add(new Label("buildNumber", "#" + getBuild().getNumber()));
		return fragment;
	}

	@Override
	protected String getPageTitle() {
		if (getBuild().getVersion() != null)
			return getBuild().getVersion() + " - Build #" +  getBuild().getNumber() + " - " + getProject().getName();
		else
			return "Build #" +  getBuild().getNumber() + " - " + getProject().getName();
	}
	
}

package io.onedev.server.web.page.project.builds.detail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramsupply.ParamSupply;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.Project;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.util.script.identity.ScriptIdentityAware;
import io.onedev.server.web.behavior.WebSocketObserver;
import io.onedev.server.web.behavior.clipboard.CopyClipboardBehavior;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.build.side.BuildSidePanel;
import io.onedev.server.web.component.build.status.BuildStatusIcon;
import io.onedev.server.web.component.commit.message.CommitMessagePanel;
import io.onedev.server.web.component.contributorpanel.ContributorPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.modal.confirm.ConfirmModal;
import io.onedev.server.web.component.sideinfo.SideInfoClosed;
import io.onedev.server.web.component.sideinfo.SideInfoOpened;
import io.onedev.server.web.component.sideinfo.SideInfoPanel;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.contributoravatars.ContributorAvatars;
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
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.util.BuildAware;
import io.onedev.server.web.util.ConfirmOnClick;
import io.onedev.server.web.util.QueryPosition;
import io.onedev.server.web.util.QueryPositionSupport;

@SuppressWarnings("serial")
public abstract class BuildDetailPage extends ProjectPage 
		implements InputContext, ScriptIdentityAware, BuildAware {

	public static final String PARAM_BUILD = "build";
	
	private static final int MAX_TABS_BEFORE_COLLAPSE = 10;
	
	protected final IModel<Build> buildModel;
	
	private final QueryPosition position;
	
	public BuildDetailPage(PageParameters params) {
		super(params);
		
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				Long buildNumber = params.get(PARAM_BUILD).toLong();
				Build build = OneDev.getInstance(BuildManager.class).find(getProject(), buildNumber);
				if (build == null)
					throw new EntityNotFoundException("Unable to find build #" + buildNumber + " in project " + getProject());
				return build;
			}

		};
	
		position = QueryPosition.from(params);
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
			public void onObservableChanged(IPartialPageRequestHandler handler, String observable) {
				handler.add(component);
				handler.appendJavaScript("$(window).resize();");
			}
			
			@Override
			public void onConnectionOpened(IPartialPageRequestHandler handler) {
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
		
		WebMarkupContainer summary = new WebMarkupContainer("summary");
		summary.add(newBuildObserver(getBuild().getId()));
		summary.setOutputMarkupId(true);
		add(summary);
		
		summary.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				StringBuilder builder = new StringBuilder("#" + getBuild().getNumber());
				if (getBuild().getVersion() != null)
					builder.append(" (" + getBuild().getVersion() + ")");
				return builder.toString();
				
			}
			
		}));
		
		summary.add(new BuildStatusIcon("statusIcon", new AbstractReadOnlyModel<Status>() {

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
		summary.add(new Label("statusLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return buildModel.getObject().getStatus().getDisplayName();
			}
			
		}));
		
		summary.add(new AjaxLink<Void>("rebuild") {

			private void resubmit(Serializable paramBean) {
				Map<String, List<String>> paramMap = ParamSupply.getParamMap(getBuild().getJob(), paramBean, 
						getBuild().getJob().getParamSpecMap().keySet());
				OneDev.getInstance(JobManager.class).resubmit(getBuild(), paramMap, SecurityUtils.getUser());
				setResponsePage(BuildDashboardPage.class, BuildDashboardPage.paramsOf(getBuild(), position));
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
					new ConfirmModal(target) {
						
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
		
		summary.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				OneDev.getInstance(JobManager.class).cancel(getBuild(), SecurityUtils.getUser());
				getSession().success("Cancel request submitted");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getBuild().isFinished() && SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName()));
			}
			
		}.add(new ConfirmOnClick("Do you really want to cancel this build?")));
		
		summary.add(new AjaxLink<Void>("moreInfo") {

			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof SideInfoClosed) {
					SideInfoClosed moreInfoSideClosed = (SideInfoClosed) event.getPayload();
					setVisible(true);
					moreInfoSideClosed.getHandler().add(this);
				}
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisible(false);
				target.add(this);
				send(getPage(), Broadcast.BREADTH, new SideInfoOpened(target));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
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
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnDomReadyHeaderItem.forScript("onedev.server.buildDetail.onErrorMessageDomReady();"));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getBuild().isFinished() && getBuild().getErrorMessage() != null);
			}
			
		});
		
		CommitDetailPage.State commitState = new CommitDetailPage.State();
		commitState.revision = getBuild().getCommitHash();
		PageParameters params = CommitDetailPage.paramsOf(projectModel.getObject(), commitState);
		
		add(new CommitMessagePanel("commitMessage", new AbstractReadOnlyModel<RevCommit>() {

			@Override
			public RevCommit getObject() {
				return getProject().getRevCommit(getBuild().getCommitHash(), true);
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canReadCode(getProject()));
			}

			@Override
			protected Project getProject() {
				return BuildDetailPage.this.getProject();
			}
			
		});
		
		RevCommit commit = getProject().getRevCommit(getBuild().getCommitHash(), true);
		add(new ContributorAvatars("commitAvatars", commit.getAuthorIdent(), commit.getCommitterIdent()));
		add(new ContributorPanel("commitNames", commit.getAuthorIdent(), commit.getCommitterIdent()));
		
		Link<Void> hashLink = new ViewStateAwarePageLink<Void>("commitHash", CommitDetailPage.class, params) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (!SecurityUtils.canReadCode(getProject()))
					tag.setName("span");
			}
			
		};
		hashLink.setEnabled(SecurityUtils.canReadCode(getProject()));
		hashLink.add(new Label("label", GitUtils.abbreviateSHA(commit.name())));
		add(hashLink);
		
		add(new WebMarkupContainer("copyCommitHash").add(new CopyClipboardBehavior(Model.of(commit.name()))));

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
				
				tabs.add(new BuildTab("Fixed Issues", FixedIssuesPage.class));
				
				if (SecurityUtils.canReadCode(getProject()))
					tabs.add(new BuildTab("Changes", BuildChangesPage.class));
				
				List<BuildTabContribution> contributions = new ArrayList<>(OneDev.getExtensions(BuildTabContribution.class));
				contributions.sort(Comparator.comparing(BuildTabContribution::getOrder));
				
				for (BuildTabContribution contribution: contributions)
					tabs.addAll(contribution.getTabs(getBuild()));
				
				return tabs;
			}
			
		}, MAX_TABS_BEFORE_COLLAPSE) {

			@Override
			public void onInitialize() {
				super.onInitialize();
				add(newBuildObserver(getBuild().getId()));
				setOutputMarkupId(true);
			}
			
		});
		
		add(new SideInfoPanel("side") {

			@Override
			protected Component newContent(String componentId) {
				return new BuildSidePanel(componentId) {

					@Override
					protected Build getBuild() {
						return BuildDetailPage.this.getBuild();
					}

					@Override
					protected QueryPositionSupport<Build> getQueryPositionSupport() {
						return new QueryPositionSupport<Build>() {

							@Override
							public QueryPosition getPosition() {
								return position;
							}

							@Override
							public void navTo(AjaxRequestTarget target, Build entity, QueryPosition position) {
								BuildDetailPage.this.navTo(target, entity, position);
							}
							
						};
					}

					@Override
					protected Component newDeleteLink(String componentId) {
						Link<Void> deleteLink = new Link<Void>(componentId) {

							@Override
							public void onClick() {
								OneDev.getInstance(BuildManager.class).delete(getBuild());
								PageParameters params = ProjectBuildsPage.paramsOf(
										getProject(), 
										QueryPosition.getQuery(position), 
										QueryPosition.getPage(position) + 1); 
								setResponsePage(ProjectBuildsPage.class, params);
							}
							
						};
						deleteLink.add(new ConfirmOnClick("Do you really want to delete this build?"));
						deleteLink.setVisible(SecurityUtils.canManage(getBuild()));
						return deleteLink;
					}
					
				};
			}
			
		});
		
	}
	
	public QueryPosition getPosition() {
		return position;
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

	public static PageParameters paramsOf(Build build, @Nullable QueryPosition position) {
		PageParameters params = ProjectPage.paramsOf(build.getProject());
		params.add(PARAM_BUILD, build.getNumber());
		if (position != null)
			position.fill(params);
		return params;
	}
	
	protected void navTo(AjaxRequestTarget target, Build entity, QueryPosition position) {
		PageParameters params = BuildDetailPage.paramsOf(entity, position);
		setResponsePage(getPageClass(), params);
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

}

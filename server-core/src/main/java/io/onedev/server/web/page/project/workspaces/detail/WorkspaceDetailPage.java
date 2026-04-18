package io.onedev.server.web.page.project.workspaces.detail;

import static io.onedev.server.web.translation.Translation._T;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.model.support.workspace.spec.ShortcutConfig;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.tabbable.Tab;
import io.onedev.server.web.component.tabbable.Tabbable;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.component.workspace.invalidspec.InvalidWorkspaceSpecIcon;
import io.onedev.server.web.component.workspace.status.WorkspaceStatusIcon;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.workspaces.ProjectWorkspacesPage;
import io.onedev.server.web.page.project.workspaces.detail.changes.WorkspaceChangesPage;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.web.page.project.workspaces.detail.log.WorkspaceLogPage;
import io.onedev.server.web.page.project.workspaces.detail.terminal.TerminalTab;
import io.onedev.server.web.page.project.workspaces.detail.terminal.WorkspaceTerminalPage;
import io.onedev.server.web.util.ConfirmClickModifier;
import io.onedev.server.web.util.Cursor;
import io.onedev.server.web.util.CursorSupport;
import io.onedev.server.workspace.WorkspaceService;

public abstract class WorkspaceDetailPage extends ProjectPage {

	private static final long serialVersionUID = 1L;

	public static final String PARAM_WOPKSPACE = "workspace";

	protected final IModel<Workspace> workspaceModel;

	@Inject
	protected WorkspaceService workspaceService;

	@Inject
	protected ClusterService clusterService;

	@Inject
	protected SettingService settingService;

	private Workspace.Status workspaceStatus;

	public WorkspaceDetailPage(PageParameters params) {
		super(params);

		String workspaceNumberString = params.get(PARAM_WOPKSPACE).toString();
		if (StringUtils.isBlank(workspaceNumberString))
			throw new RestartResponseException(ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(getProject().getId()));

		workspaceModel = new LoadableDetachableModel<Workspace>() {

			@Override
			protected Workspace load() {
				Long workspaceNumber = Long.parseLong(workspaceNumberString);
				Workspace workspace = workspaceService.find(getProject(), workspaceNumber);
				if (workspace == null)
					throw new EntityNotFoundException(_T("Workspace not found"));
				if (!workspace.getProject().equals(getProject()))
					throw new RestartResponseException(WorkspaceDashboardPage.class, paramsOf(workspace));
				return workspace;
			}

		};
	}

	public Workspace getWorkspace() {
		return workspaceModel.getObject();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canWriteCode(getProject());
	}

	@Override
	protected void onDetach() {
		workspaceModel.detach();
		super.onDetach();
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		return new ViewStateAwarePageLink<>(componentId, ProjectDashboardPage.class,
				ProjectDashboardPage.paramsOf(project.getId()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Workspace workspace = getWorkspace();

		var head = new WebMarkupContainer("head");
		add(head);

		head.add(new UserIdentPanel("user", workspace.getUser(), Mode.AVATAR_AND_NAME));
		head.add(new BranchLink("branch", new ProjectAndBranch(workspace.getProject(), workspace.getBranch()), true));
		head.add(new Label("spec", workspace.getSpecName()));
		head.add(new InvalidWorkspaceSpecIcon("invalidSpec", workspaceModel));

		head.add(new WorkspaceStatusIcon("statusIcon", new AbstractReadOnlyModel<Status>() {

			@Override
			public Status getObject() {
				return getWorkspace().getStatus();
			}
	
		}));
		head.add(new Label("statusLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T(workspaceModel.getObject().getStatus().toString());
			}

		}));

		head.add(new MenuLink("shortcuts") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				var menuItems = new ArrayList<MenuItem>();
				for (var shortcut : getShortcutConfigs()) {
					final String name = shortcut.getName();
					final String command = shortcut.getCommand();
					menuItems.add(new MenuItem() {
						
						@Override
						public String getLabel() {
							return name;
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new Link<Void>(id) {
								@Override
								public void onClick() {
									var shellId = workspaceService.openShell(getWorkspace(), name);
									setResponsePage(WorkspaceTerminalPage.class,
											WorkspaceTerminalPage.paramsOf(getWorkspace(), shellId, command));
								}
							};
						
						}

					});
				}
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getWorkspace().getStatus() == Status.ACTIVE
						&& SecurityUtils.canModifyOrDelete(getWorkspace())
						&& !getShortcutConfigs().isEmpty());
			}

		});

		head.add(new Link<Void>("openTerminal") {

			@Override
			public void onClick() {
				var shellId = workspaceService.openShell(getWorkspace(), _T("Terminal"));
				setResponsePage(WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(getWorkspace(), shellId));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getWorkspace().getStatus() == Status.ACTIVE
					&& SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		head.add(new MenuLink("portMappings") {

			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				Long projectId = getProject().getId();
				var serverHost = projectService.runOnActiveServer(projectId, () -> {
					return clusterService.getServerHost(clusterService.getLocalServerAddress());
				});
				try {
					if (InetAddress.getByName(serverHost).isLoopbackAddress()) {
						try {
							serverHost = new URL(settingService.getSystemSetting().getServerUrl()).getHost();
						} catch (MalformedURLException e) {
							throw new RuntimeException(e);
						}	
					}
				} catch (UnknownHostException e) {
					throw new RuntimeException(e);
				}

				var menuItems = new ArrayList<MenuItem>();
				var mappings = workspaceService.getPortMappings(getWorkspace());
				for (var entry : mappings.entrySet()) {
					var containerPort = entry.getKey();
					var hostPort = entry.getValue();
					var url = "http://" + serverHost + ":" + hostPort;
					menuItems.add(new MenuItem() {
						@Override
						public String getLabel() {
							return "Port " + containerPort + " \u2192 " + hostPort;
						}

						@Override
						public WebMarkupContainer newLink(String id) {
							return new ExternalLink(id, url) {
								
								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									tag.put("target", "_blank");
								}

							};
						}
					});
				}
				return menuItems;
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getWorkspace().getStatus() == Status.ACTIVE
						&& !workspaceService.getPortMappings(getWorkspace()).isEmpty());
			}

		});

		head.add(new Link<Void>("reprovision") {

			@Override
			public void onClick() {
				workspaceService.requestToReprovision(getWorkspace());
				setResponsePage(WorkspaceLogPage.class, getPageParameters());
				Session.get().success(MessageFormat.format(_T("Workspace reprovisioning requested"),
						getWorkspace().getReference().toString(getWorkspace().getProject())));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getWorkspace().getStatus() == Status.ERROR
						&& SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		head.add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				workspaceService.delete(getWorkspace());
				var oldAuditContent = VersionedXmlDoc.fromBean(getWorkspace()).toXML();
				auditService.audit(getWorkspace().getProject(), "deleted workspace \"" + getWorkspace().getReference().toString(getWorkspace().getProject()) + "\"", oldAuditContent, null);
				
				Session.get().success(MessageFormat.format(_T("Workspace {0} deleted"), getWorkspace().getReference().toString(getWorkspace().getProject())));
				
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(Workspace.class);
				if (redirectUrlAfterDelete != null)
					throw new RedirectToUrlException(redirectUrlAfterDelete);
				else
					setResponsePage(ProjectWorkspacesPage.class, ProjectWorkspacesPage.paramsOf(getProject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		}.add(new ConfirmClickModifier(_T("Do you really want to delete this workspace?"))));

		head.add(new EntityNavPanel<Workspace>("entityNav") {

			@Override
			protected EntityQuery<Workspace> parse(String queryString, @Nullable Project project) {
				return WorkspaceQuery.parse(project, queryString, true);
			}

			@Override
			protected Workspace getEntity() {
				return getWorkspace();
			}

			@Override
			protected CursorSupport<Workspace> getCursorSupport() {
				return new CursorSupport<Workspace>() {
					@Override
					public Cursor getCursor() {
						return WebSession.get().getWorkspaceCursor();
					}
					@Override
					public void navTo(AjaxRequestTarget target, Workspace entity, Cursor cursor) {
						WebSession.get().setWorkspaceCursor(cursor);
						setResponsePage(WorkspaceDashboardPage.class, WorkspaceDetailPage.paramsOf(entity));
					}
				};
			}

			@Override
			protected List<Workspace> query(EntityQuery<Workspace> query, int offset, int count, @Nullable ProjectScope projectScope) {
				Project project = projectScope != null ? projectScope.getProject() : null;
				return workspaceService.query(SecurityUtils.getSubject(), project, (WorkspaceQuery) query, offset, count);
			}
		});		
		
		head.add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Set.of(getWorkspace().getStatusChangeObservable());
			}

		});

		add(new Tabbable("workspaceTabs", new LoadableDetachableModel<>() {

			@Override
			protected List<Tab> load() {
				List<Tab> tabs = new ArrayList<>();

				var labels = workspaceService.getShellLabels(getWorkspace());
				var labelCounts = new HashMap<String, Integer>();
				for (var entry: labels.entrySet()) {
					var shellId = entry.getKey();
					var label = entry.getValue();
					int count = labelCounts.merge(label, 1, Integer::sum);
					var displayLabel = count == 1 ? label : label + " " + count;
					tabs.add(new TerminalTab(getWorkspace(), shellId, displayLabel));
				}

				if (getWorkspace().getStatus() != Workspace.Status.PENDING)
					tabs.add(new WorkspaceTab(Model.of(_T("Changes")), Model.of("diff"), WorkspaceChangesPage.class, WorkspaceChangesPage.paramsOf(getWorkspace())));
				tabs.add(new WorkspaceTab(Model.of(_T("Log")), Model.of("log"), WorkspaceLogPage.class, WorkspaceLogPage.paramsOf(getWorkspace())));
				return tabs;
			}

		}).add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Set.of(getWorkspace().getStatusChangeObservable());
			}
			
		}));

		workspaceStatus = getWorkspace().getStatus();
		add(new ChangeObserver() {

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				if (workspaceStatus != Workspace.Status.ACTIVE && getWorkspace().getStatus() == Workspace.Status.ACTIVE) {
					var firstShortcut = getShortcutConfigs().stream().findFirst().orElse(null);
					var command = firstShortcut != null ? firstShortcut.getCommand() : null;
					var label = firstShortcut != null ? firstShortcut.getName() : _T("Terminal");
					var shellId = workspaceService.openShell(getWorkspace(), label);
					setResponsePage(WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(getWorkspace(), shellId, command));
				} else if (workspaceStatus == Workspace.Status.ACTIVE && getWorkspace().getStatus() != Workspace.Status.ACTIVE) {
					setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(getWorkspace()));
				}
			}

			@Override
			public Collection<String> findObservables() {
				return Set.of(getWorkspace().getStatusChangeObservable());
			}

		});	
		
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new WorkspaceDetailCssResourceReference()));
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		Fragment fragment = new Fragment(componentId, "projectTitleFrag", this);
		fragment.add(new ViewStateAwarePageLink<Void>("workspaces", ProjectWorkspacesPage.class,
				ProjectWorkspacesPage.paramsOf(getProject(), 0)));
		fragment.add(new Label("workspaceNumber", getWorkspace().getReference().toString(getProject())));
		return fragment;
	}

	public static PageParameters paramsOf(Workspace workspace) {
		return paramsOf(workspace.getProject(), workspace.getNumber());
	}

	public static PageParameters paramsOf(Project project, Long workspaceNumber) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_WOPKSPACE, workspaceNumber);
		return params;
	}

	private List<ShortcutConfig> getShortcutConfigs() {
		var spec = getProject().getHierarchyWorkspaceSpecs().stream()
			.filter(it -> it.getName().equals(getWorkspace().getSpecName()))
			.findFirst()
			.orElse(null);			
		return spec != null? spec.getShortcutConfigs(): List.of();
	}

}

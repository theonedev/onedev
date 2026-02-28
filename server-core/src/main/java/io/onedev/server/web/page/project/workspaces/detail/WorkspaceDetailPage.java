package io.onedev.server.web.page.project.workspaces.detail;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import org.jspecify.annotations.Nullable;

import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.model.Workspace.Status;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.workspace.WorkspaceQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.component.branch.BranchLink;
import io.onedev.server.web.component.entity.nav.EntityNavPanel;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
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

		add(new UserIdentPanel("user", workspace.getUser(), Mode.AVATAR_AND_NAME));
		add(new BranchLink("branch", new ProjectAndBranch(workspace.getProject(), workspace.getBranch()), true));
		add(new Label("spec", workspace.getSpecName()));
		add(new InvalidWorkspaceSpecIcon("invalidSpec", workspaceModel));

		add(new WorkspaceStatusIcon("statusIcon", new AbstractReadOnlyModel<Status>() {

			@Override
			public Status getObject() {
				return getWorkspace().getStatus();
			}
	
		}) {

			@Override
			protected Collection<String> getChangeObservables() {
				return Set.of(getWorkspace().getStatusChangeObservable());
			}

		});
		add(new Label("statusLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return _T(workspaceModel.getObject().getStatus().toString());
			}

		}).add(new ChangeObserver() {

			@Override
			public Collection<String> findObservables() {
				return Set.of(getWorkspace().getStatusChangeObservable());
			}

		}));

		add(new Link<Void>("openTerminal") {

			@Override
			public void onClick() {
				var terminalIndex = workspaceService.openShell(getWorkspace());
				setResponsePage(WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(getWorkspace(), terminalIndex));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getWorkspace().getStatus() == Status.ACTIVE
					&& SecurityUtils.canModifyOrDelete(getWorkspace()));
			}

		});

		add(new Link<Void>("delete") {

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

		add(new Tabbable("workspaceTabs", new LoadableDetachableModel<>() {

			@Override
			protected List<Tab> load() {
				List<Tab> tabs = new ArrayList<>();

				for (var shellId : workspaceService.getShellIds(getWorkspace())) 
					tabs.add(new TerminalTab(getWorkspace(), shellId));
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

		add(new EntityNavPanel<Workspace>("entityNav") {

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
		
		workspaceStatus = getWorkspace().getStatus();
		add(new ChangeObserver() {

			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				if (workspaceStatus == Workspace.Status.PENDING && getWorkspace().getStatus() == Workspace.Status.ACTIVE) {
					var terminalId = workspaceService.openShell(getWorkspace());
					setResponsePage(WorkspaceTerminalPage.class, WorkspaceTerminalPage.paramsOf(getWorkspace(), terminalId));
				} else if (workspaceStatus == Workspace.Status.ACTIVE && getWorkspace().getStatus() == Workspace.Status.ERROR) {
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

}

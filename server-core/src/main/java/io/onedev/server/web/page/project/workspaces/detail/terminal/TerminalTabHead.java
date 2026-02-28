package io.onedev.server.web.page.project.workspaces.detail.terminal;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;
import io.onedev.server.web.page.project.workspaces.detail.dashboard.WorkspaceDashboardPage;
import io.onedev.server.workspace.WorkspaceService;

public class TerminalTabHead extends Panel {

	private static final long serialVersionUID = 1L;

	private final TerminalTab tab;

	private final String shellId;

	@Inject
	private WorkspaceService workspaceService;

	public TerminalTabHead(String id, TerminalTab tab, String shellId) {
		super(id);
		this.tab = tab;
		this.shellId = shellId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		var page = (WorkspaceDetailPage) getPage();
		Link<Void> link = new ViewStateAwarePageLink<>("link", WorkspaceTerminalPage.class,
				WorkspaceTerminalPage.paramsOf(page.getWorkspace(), shellId));
		add(link);
		link.add(new SpriteImage("icon", tab.getIconModel()));
		link.add(new Label("label", tab.getTitleModel()));

		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var page = (WorkspaceDetailPage) getPage();
				var workspace = page.getWorkspace();
				workspaceService.terminateShell(workspace, shellId);

				if (page instanceof WorkspaceTerminalPage terminalPage && shellId.equals(terminalPage.getShellId())) {
					setResponsePage(WorkspaceDashboardPage.class, WorkspaceDashboardPage.paramsOf(workspace));
				} else {
					setResponsePage(getPage().getClass(), getPage().getPageParameters());
				}
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				var workspace = ((WorkspaceDetailPage) getPage()).getWorkspace();
				setVisible(SecurityUtils.canModifyOrDelete(workspace));
			}

		});

		add(new WebMarkupContainer("readOnlyNotice").setVisible(!SecurityUtils.canModifyOrDelete(page.getWorkspace())));
	}

}

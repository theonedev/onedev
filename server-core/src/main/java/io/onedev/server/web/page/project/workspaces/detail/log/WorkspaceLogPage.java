package io.onedev.server.web.page.project.workspaces.detail.log;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.model.Project;
import io.onedev.server.model.Workspace;
import io.onedev.server.web.component.logging.LogPanel;
import io.onedev.server.web.component.logging.PauseSupport;
import io.onedev.server.web.page.project.workspaces.detail.WorkspaceDetailPage;

public class WorkspaceLogPage extends WorkspaceDetailPage {

	public WorkspaceLogPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new LogPanel("log") {

			@Override
			protected LoggingSupport getLoggingSupport() {
				return getWorkspace().getLoggingSupport();
			}

			@Nullable
			@Override
			protected PauseSupport getPauseSupport() {
				return null;
			}

		});
	}

	public static PageParameters paramsOf(Workspace workspace) {
		return WorkspaceDetailPage.paramsOf(workspace);
	}

	public static PageParameters paramsOf(Project project, Long workspaceNumber) {
		return WorkspaceDetailPage.paramsOf(project, workspaceNumber);
	}

}

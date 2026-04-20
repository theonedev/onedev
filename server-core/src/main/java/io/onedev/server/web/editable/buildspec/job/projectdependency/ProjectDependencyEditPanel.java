package io.onedev.server.web.editable.buildspec.job.projectdependency;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

abstract class ProjectDependencyEditPanel extends DrawCardBeanItemEditPanel<ProjectDependency>
		implements BuildSpecAware, JobAware {

	private static final long serialVersionUID = 1L;

	ProjectDependencyEditPanel(String id, List<ProjectDependency> dependencies, int dependencyIndex,
			EditCallback callback) {
		super(id, dependencies, dependencyIndex, callback);
	}

	@Override
	protected ProjectDependency newItem() {
		return new ProjectDependency();
	}

	@Override
	protected String getTitle() {
		return _T("Project Dependency");
	}

}

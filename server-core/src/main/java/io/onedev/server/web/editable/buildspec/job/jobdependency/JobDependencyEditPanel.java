package io.onedev.server.web.editable.buildspec.job.jobdependency;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

public abstract class JobDependencyEditPanel extends DrawCardBeanItemEditPanel<JobDependency>
		implements BuildSpecAware, JobAware {

	private static final long serialVersionUID = 1L;

	public JobDependencyEditPanel(String id, List<JobDependency> dependencies, int dependencyIndex, EditCallback callback) {
		super(id, dependencies, dependencyIndex, callback);
	}

	@Override
	protected JobDependency newItem() {
		return new JobDependency();
	}

	@Override
	protected String getTitle() {
		return _T("Job Dependency");
	}

	@Override
	protected void validateItem(BeanEditor editor, JobDependency item) {
		List<JobDependency> dependencies = getItems();
		int dependencyIndex = getItemIndex();
		for (int i = 0; i < dependencies.size(); i++) {
			if (i == dependencyIndex)
				continue;
			if (item.getJobName() != null && item.getJobName().equals(dependencies.get(i).getJobName())) {
				editor.error(new Path(new PathNode.Named("jobName")),
						"Dependency to this job is already defined");
				break;
			}
		}
	}

}

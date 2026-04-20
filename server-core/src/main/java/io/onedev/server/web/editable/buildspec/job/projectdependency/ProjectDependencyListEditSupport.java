package io.onedev.server.web.editable.buildspec.job.projectdependency;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class ProjectDependencyListEditSupport extends DrawCardBeanListEditSupport<ProjectDependency> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<ProjectDependency> getElementClass() {
		return ProjectDependency.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<ProjectDependency> newListViewPanel(String id, List<Serializable> elements) {
		return new ProjectDependencyListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<ProjectDependency> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new ProjectDependencyListEditPanel(id, descriptor, model);
	}

}

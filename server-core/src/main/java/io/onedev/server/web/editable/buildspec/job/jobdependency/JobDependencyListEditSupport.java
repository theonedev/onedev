package io.onedev.server.web.editable.buildspec.job.jobdependency;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class JobDependencyListEditSupport extends DrawCardBeanListEditSupport<JobDependency> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<JobDependency> getElementClass() {
		return JobDependency.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<JobDependency> newListViewPanel(String id, List<Serializable> elements) {
		return new JobDependencyListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<JobDependency> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new JobDependencyListEditPanel(id, descriptor, model);
	}

}

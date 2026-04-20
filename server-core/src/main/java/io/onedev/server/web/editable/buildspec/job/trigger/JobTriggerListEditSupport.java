package io.onedev.server.web.editable.buildspec.job.trigger;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class JobTriggerListEditSupport extends DrawCardBeanListEditSupport<JobTrigger> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<JobTrigger> getElementClass() {
		return JobTrigger.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<JobTrigger> newListViewPanel(String id, List<Serializable> elements) {
		return new JobTriggerListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<JobTrigger> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new JobTriggerListEditPanel(id, descriptor, model);
	}

}

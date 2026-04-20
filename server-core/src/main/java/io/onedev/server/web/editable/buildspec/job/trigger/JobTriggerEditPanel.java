package io.onedev.server.web.editable.buildspec.job.trigger;

import java.io.Serializable;
import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

public abstract class JobTriggerEditPanel extends DrawCardBeanItemEditPanel<JobTrigger> implements JobAware {

	private static final long serialVersionUID = 1L;

	public JobTriggerEditPanel(String id, List<JobTrigger> triggers, int triggerIndex, EditCallback callback) {
		super(id, triggers, triggerIndex, callback);
	}

	@Override
	protected JobTrigger newItem() {
		return null;
	}

	@Override
	protected String getTitle() {
		return _T("Trigger");
	}

	@Override
	protected Serializable newEditingBean(JobTrigger item) {
		JobTriggerBean bean = new JobTriggerBean();
		bean.setTrigger(item);
		return bean;
	}

	@Override
	protected JobTrigger extractItem(Serializable editingBean) {
		return ((JobTriggerBean) editingBean).getTrigger();
	}

}

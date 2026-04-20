package io.onedev.server.web.editable.servicelocator;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

abstract class ServiceLocatorEditPanel extends DrawCardBeanItemEditPanel<ServiceLocator>
		implements BuildSpecAware, JobAware {

	private static final long serialVersionUID = 1L;

	ServiceLocatorEditPanel(String id, List<ServiceLocator> locators, int locatorIndex, EditCallback callback) {
		super(id, locators, locatorIndex, callback);
	}

	@Override
	protected ServiceLocator newItem() {
		return new ServiceLocator();
	}

	@Override
	protected String getTitle() {
		return _T("Service Locator");
	}

}

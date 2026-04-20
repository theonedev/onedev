package io.onedev.server.web.editable.servicelocator;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.support.administration.jobexecutor.ServiceLocator;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class ServiceLocatorListEditSupport extends DrawCardBeanListEditSupport<ServiceLocator> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<ServiceLocator> getElementClass() {
		return ServiceLocator.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<ServiceLocator> newListViewPanel(String id, List<Serializable> elements) {
		return new ServiceLocatorListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<ServiceLocator> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new ServiceLocatorListEditPanel(id, descriptor, model);
	}

}

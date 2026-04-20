package io.onedev.server.web.editable.workspacespec.envvar;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.support.workspace.spec.EnvVar;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class EnvVarListEditSupport extends DrawCardBeanListEditSupport<EnvVar> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<EnvVar> getElementClass() {
		return EnvVar.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<EnvVar> newListViewPanel(String id, List<Serializable> elements) {
		return new EnvVarListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<EnvVar> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new EnvVarListEditPanel(id, descriptor, model);
	}

}

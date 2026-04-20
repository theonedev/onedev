package io.onedev.server.web.editable.buildspec.param.spec;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class ParamSpecListEditSupport extends DrawCardBeanListEditSupport<ParamSpec> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<ParamSpec> getElementClass() {
		return ParamSpec.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<ParamSpec> newListViewPanel(String id, List<Serializable> elements) {
		return new ParamSpecListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<ParamSpec> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new ParamSpecListEditPanel(id, descriptor, model);
	}

}

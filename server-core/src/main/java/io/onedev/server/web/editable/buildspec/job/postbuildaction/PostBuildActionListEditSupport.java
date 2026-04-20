package io.onedev.server.web.editable.buildspec.job.postbuildaction;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class PostBuildActionListEditSupport extends DrawCardBeanListEditSupport<PostBuildAction> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<PostBuildAction> getElementClass() {
		return PostBuildAction.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<PostBuildAction> newListViewPanel(String id, List<Serializable> elements) {
		return new PostBuildActionListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<PostBuildAction> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new PostBuildActionListEditPanel(id, descriptor, model);
	}

}

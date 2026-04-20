package io.onedev.server.web.editable.workspacespec.userdata;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class UserDataListEditSupport extends DrawCardBeanListEditSupport<UserData> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<UserData> getElementClass() {
		return UserData.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<UserData> newListViewPanel(String id, List<Serializable> elements) {
		return new UserDataListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<UserData> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new UserDataListEditPanel(id, descriptor, model);
	}

}

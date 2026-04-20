package io.onedev.server.web.editable.workspacespec.userdata;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

class UserDataEditPanel extends DrawCardBeanItemEditPanel<UserData> {

	private static final long serialVersionUID = 1L;

	UserDataEditPanel(String id, List<UserData> configs, int configIndex, EditCallback callback) {
		super(id, configs, configIndex, callback);
	}

	@Override
	protected UserData newItem() {
		return new UserData();
	}

	@Override
	protected String getTitle() {
		return _T("User Data");
	}

}

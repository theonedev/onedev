package io.onedev.server.web.editable.workspacespec.envvar;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.model.support.workspace.spec.EnvVar;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

class EnvVarEditPanel extends DrawCardBeanItemEditPanel<EnvVar> {

	private static final long serialVersionUID = 1L;

	EnvVarEditPanel(String id, List<EnvVar> envVars, int envVarIndex, EditCallback callback) {
		super(id, envVars, envVarIndex, callback);
	}

	@Override
	protected EnvVar newItem() {
		return new EnvVar();
	}

	@Override
	protected String getTitle() {
		return _T("Environment Variable");
	}

}

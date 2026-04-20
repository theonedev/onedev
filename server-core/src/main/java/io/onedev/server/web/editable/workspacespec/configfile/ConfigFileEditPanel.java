package io.onedev.server.web.editable.workspacespec.configfile;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

class ConfigFileEditPanel extends DrawCardBeanItemEditPanel<ConfigFile> {

	private static final long serialVersionUID = 1L;

	ConfigFileEditPanel(String id, List<ConfigFile> configFiles, int configFileIndex, EditCallback callback) {
		super(id, configFiles, configFileIndex, callback);
	}

	@Override
	protected ConfigFile newItem() {
		return new ConfigFile();
	}

	@Override
	protected String getTitle() {
		return _T("Config File");
	}

}

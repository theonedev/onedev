package io.onedev.server.web.editable.workspacespec.configfile;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class ConfigFileListEditSupport extends DrawCardBeanListEditSupport<ConfigFile> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<ConfigFile> getElementClass() {
		return ConfigFile.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<ConfigFile> newListViewPanel(String id, List<Serializable> elements) {
		return new ConfigFileListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<ConfigFile> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new ConfigFileListEditPanel(id, descriptor, model);
	}

}

package io.onedev.server.web.editable.workspacespec.cacheconfig;

import java.util.List;

import static io.onedev.server.web.translation.Translation._T;

import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;

class CacheConfigEditPanel extends DrawCardBeanItemEditPanel<CacheConfig> {

	private static final long serialVersionUID = 1L;

	CacheConfigEditPanel(String id, List<CacheConfig> configs, int configIndex, EditCallback callback) {
		super(id, configs, configIndex, callback);
	}

	@Override
	protected CacheConfig newItem() {
		return new CacheConfig();
	}

	@Override
	protected String getTitle() {
		return _T("Cache Config");
	}

}

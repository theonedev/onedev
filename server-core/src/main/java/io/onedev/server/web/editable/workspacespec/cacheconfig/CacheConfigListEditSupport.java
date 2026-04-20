package io.onedev.server.web.editable.workspacespec.cacheconfig;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.model.IModel;

import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditSupport;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

public class CacheConfigListEditSupport extends DrawCardBeanListEditSupport<CacheConfig> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Class<CacheConfig> getElementClass() {
		return CacheConfig.class;
	}

	@Override
	protected DrawCardBeanListViewPanel<CacheConfig> newListViewPanel(String id, List<Serializable> elements) {
		return new CacheConfigListViewPanel(id, elements);
	}

	@Override
	protected DrawCardBeanListEditPanel<CacheConfig> newListEditPanel(String id, PropertyDescriptor descriptor,
			IModel<List<Serializable>> model) {
		return new CacheConfigListEditPanel(id, descriptor, model);
	}

}

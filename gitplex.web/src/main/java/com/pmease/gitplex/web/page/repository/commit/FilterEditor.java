package com.pmease.gitplex.web.page.repository.commit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public abstract class FilterEditor<T extends CommitFilter> extends GenericPanel<T> {

	public FilterEditor(String id, IModel<T> filterModel) {
		super(id, filterModel);
	}

	public abstract void onEdit(AjaxRequestTarget target);
}

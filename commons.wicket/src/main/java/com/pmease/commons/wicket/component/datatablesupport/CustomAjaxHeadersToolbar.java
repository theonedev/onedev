package com.pmease.commons.wicket.component.datatablesupport;

import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;

import com.pmease.commons.wicket.ajaxlistener.AjaxLoadingOverlay;

@SuppressWarnings("serial")
public class CustomAjaxHeadersToolbar<S> extends AjaxFallbackHeadersToolbar<S>{

	public CustomAjaxHeadersToolbar(DataTable<?, S> table, ISortStateLocator<S> stateLocator) {
		super(table, stateLocator);
	}

	@Override
	protected IAjaxCallListener getAjaxCallListener() {
		return new AjaxLoadingOverlay();
	}

}

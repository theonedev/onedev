package io.onedev.server.web.component.pipeline;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface Sortable extends Serializable {

	void onSort(AjaxRequestTarget target, int fromIndex, int toIndex);
	
}

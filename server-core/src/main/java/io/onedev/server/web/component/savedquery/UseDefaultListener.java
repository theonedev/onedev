package io.onedev.server.web.component.savedquery;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

interface UseDefaultListener extends Serializable {
	
	void onUseDefault(AjaxRequestTarget target);
	
}

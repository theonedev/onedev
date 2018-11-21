package io.onedev.server.web.component.issue.statetransition;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface ChangeListener extends Serializable {
	
	void onChanged(AjaxRequestTarget target);
	
}

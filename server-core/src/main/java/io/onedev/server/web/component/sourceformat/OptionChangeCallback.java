package io.onedev.server.web.component.sourceformat;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface OptionChangeCallback extends Serializable {
	
	void onOptioneChange(AjaxRequestTarget target);
	
}

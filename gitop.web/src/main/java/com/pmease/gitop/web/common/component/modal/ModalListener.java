package com.pmease.gitop.web.common.component.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.io.IClusterable;

public interface ModalListener extends IClusterable {
	
	void onShown(Modal modal, AjaxRequestTarget target);
	
	void onHidden(Modal modal, AjaxRequestTarget target);
	
}

package com.pmease.commons.wicket.editable;

import org.apache.wicket.Component;

import com.pmease.commons.editable.EditContext;

public interface RenderableEditContext extends EditContext {
	
	Component renderForEdit(String componentId);
	
	Component renderForView(String componentId);

}

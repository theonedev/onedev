package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import org.apache.wicket.Component;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditSupportRegistry;
import com.pmease.commons.loader.AppLoader;

public class EditHelper {
	
	public static EditContext getContext(Serializable bean) {
		return AppLoader.getInstance(EditSupportRegistry.class).getBeanEditContext(bean);
	}
	
	public static EditContext getContext(Serializable bean, String propertyName) {
		return AppLoader.getInstance(EditSupportRegistry.class).getPropertyEditContext(bean, propertyName);
	}

	public static Component renderForEdit(EditContext editContext, String componentId) {
		return (Component) editContext.renderForEdit(componentId);
	}
	
	public static Component renderForView(EditContext editContext, String componentId) {
		return (Component) editContext.renderForView(componentId);
	}
	
}

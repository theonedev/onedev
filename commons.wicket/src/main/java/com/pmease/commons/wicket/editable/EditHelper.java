package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import com.pmease.commons.editable.EditSupportRegistry;
import com.pmease.commons.loader.AppLoader;

public class EditHelper {
	
	public static RenderableEditContext getContext(Serializable bean) {
		return (RenderableEditContext) AppLoader.getInstance(EditSupportRegistry.class).getBeanEditContext(bean);
	}
	
	public static RenderableEditContext getContext(Serializable bean, String propertyName) {
		return (RenderableEditContext) AppLoader.getInstance(EditSupportRegistry.class).getPropertyEditContext(bean, propertyName);
	}

}

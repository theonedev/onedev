package com.pmease.commons.wicket.editable;

import java.io.Serializable;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditSupportRegistry;
import com.pmease.commons.loader.AppLoader;

public class EditHelper {
	
	@SuppressWarnings("unchecked")
	public static EditContext<RenderContext> getContext(Serializable bean) {
		return AppLoader.getInstance(EditSupportRegistry.class).getBeanEditContext(bean);
	}
	
	@SuppressWarnings("unchecked")
	public static EditContext<RenderContext> getContext(Serializable bean, String propertyName) {
		return AppLoader.getInstance(EditSupportRegistry.class).getPropertyEditContext(bean, propertyName);
	}

}

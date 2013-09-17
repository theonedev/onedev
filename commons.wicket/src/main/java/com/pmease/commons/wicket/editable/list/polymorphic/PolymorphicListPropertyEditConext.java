package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;

import com.pmease.commons.editable.AbstractPolymorphicListPropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditConext extends AbstractPolymorphicListPropertyEditContext<RenderContext> {

	public PolymorphicListPropertyEditConext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		// TODO Auto-generated method stub
		
	}

}

package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.AbstractReflectionPropertyEditContext;
import com.pmease.commons.editable.EditContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditContext extends AbstractReflectionPropertyEditContext {

	public ReflectionPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new ReflectionPropertyEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		EditContext valueContext = getValueContext();
		if (valueContext != null) {
			return valueContext.renderForView(renderParam);
		} else {
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}

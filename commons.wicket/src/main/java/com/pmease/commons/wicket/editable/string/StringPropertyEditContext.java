package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.PropertyEditContext;

@SuppressWarnings("serial")
public class StringPropertyEditContext extends PropertyEditContext {

	public StringPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		return new StringPropertyEditor((String) renderParam, this);
	}

	@Override
	public Object renderForView(Object renderParam) {
		if (getPropertyValue() != null)
			return new Label((String) renderParam, (String) getPropertyValue());
		else
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
	}

	@Override
	public Map<Serializable, EditContext> getChildContexts() {
		return null;
	}

}

package com.pmease.commons.wicket.editable.bool;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;
import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.EditableHeaderItem;

@SuppressWarnings("serial")
public class NullableBooleanPropertyEditContext extends PropertyEditContext {

	public NullableBooleanPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public Object renderForEdit(Object renderParam) {
		DropDownChoice<String> dropDownChoice = new DropDownChoice<String>((String) renderParam, new IModel<String>() {

			public void detach() {
			}

			public String getObject() {
				Boolean propertyValue = (Boolean) getPropertyValue();
				if (propertyValue != null) {
					if (propertyValue)
						return "yes";
					else
						return "no";
				} else {
					return null;
				}
			}

			public void setObject(String object) {
				if ("yes".equals(object))
					setPropertyValue(true);
				else if ("no".equals(object))
					setPropertyValue(false);
				else
					setPropertyValue(null);
			}
			
		}, Lists.newArrayList("yes", "no")) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("select");
				tag.put("class", "form-control");
				super.onComponentTag(tag);
			}
			
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(EditableHeaderItem.get());
			}

		};
		
		dropDownChoice.setNullValid(true);
		
		return dropDownChoice;
	}

	@Override
	public Object renderForView(Object renderParam) {
		Boolean propertyValue = (Boolean) getPropertyValue();
		if (propertyValue != null) {
			if (propertyValue)
				return new Label((String) renderParam, "yes");
			else
				return new Label((String) renderParam, "no");
		} else {
			return new Label((String) renderParam, "<i>Not Defined</i>").setEscapeModelStrings(false);
		}
	}

}

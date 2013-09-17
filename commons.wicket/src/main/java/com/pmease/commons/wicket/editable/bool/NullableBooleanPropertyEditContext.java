package com.pmease.commons.wicket.editable.bool;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.util.EasyList;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class NullableBooleanPropertyEditContext extends PropertyEditContext<RenderContext> {

	public NullableBooleanPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		
		DropDownChoice<String> dropDownChoice = new DropDownChoice<String>(renderContext.getComponentId(), new IModel<String>() {

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
			
		}, EasyList.of("yes", "no")) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("select");
				super.onComponentTag(tag);
			}
			
		};
		
		dropDownChoice.setNullValid(true);
		
		renderContext.getContainer().add(dropDownChoice);
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		Boolean propertyValue = (Boolean) getPropertyValue();
		if (propertyValue != null) {
			if (propertyValue)
				renderContext.getContainer().add(new Label(renderContext.getComponentId(), "yes"));
			else
				renderContext.getContainer().add(new Label(renderContext.getComponentId(), "no"));
		} else {
			renderContext.getContainer().add(new Label(renderContext.getComponentId()));
		}
	}

}

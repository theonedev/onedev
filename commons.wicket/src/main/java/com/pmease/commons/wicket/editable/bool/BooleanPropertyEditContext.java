package com.pmease.commons.wicket.editable.bool;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class BooleanPropertyEditContext extends PropertyEditContext<RenderContext> {

	public BooleanPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new CheckBox(renderContext.getComponentId(), new IModel<Boolean>() {

			@Override
			public void detach() {
			}

			@Override
			public Boolean getObject() {
				return (Boolean) getPropertyValue();
			}

			@Override
			public void setObject(Boolean object) {
				setPropertyValue(object);
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("input");
				tag.put("type", "checkbox");
				super.onComponentTag(tag);
			}
			
		});
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		renderContext.getContainer().add(new Label(renderContext.getComponentId(), getPropertyValue().toString()));
	}

}

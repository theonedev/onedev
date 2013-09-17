package com.pmease.commons.wicket.editable.string;

import java.io.Serializable;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.PropertyEditContext;
import com.pmease.commons.wicket.editable.RenderContext;

@SuppressWarnings("serial")
public class StringPropertyEditContext extends PropertyEditContext<RenderContext> {

	public StringPropertyEditContext(Serializable bean, String propertyName) {
		super(bean, propertyName);
	}

	@Override
	public void renderForEdit(RenderContext renderContext) {
		renderContext.getContainer().add(new TextField<String>(renderContext.getComponentId(), new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return (String) getPropertyValue();
			}

			@Override
			public void setObject(String object) {
				setPropertyValue(object);
			}
			
		}) {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				tag.setName("input");
				tag.put("type", "text");
				super.onComponentTag(tag);
			}
			
		});
	}

	@Override
	public void renderForView(RenderContext renderContext) {
		renderContext.getContainer().add(new Label(renderContext.getComponentId(), (String) getPropertyValue()));
	}

}

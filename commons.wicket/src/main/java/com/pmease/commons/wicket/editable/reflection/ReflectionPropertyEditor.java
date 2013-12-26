package com.pmease.commons.wicket.editable.reflection;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.EditContext;

@SuppressWarnings("serial")
public class ReflectionPropertyEditor extends Panel {

	private final ReflectionPropertyEditContext editContext;
	
	private final String VALUE_EDITOR_ID = "valueEditor";
	
	public ReflectionPropertyEditor(String id, ReflectionPropertyEditContext editContext) {
		super(id);
		this.editContext = editContext;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (editContext.isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
			if (editContext.getPropertyValue() == null) {
				editContext.setPropertyValue(editContext.instantiate(editContext.getPropertyGetter().getReturnType()));
			}
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					return editContext.getPropertyValue() != null;
				}
	
				@Override
				public void setObject(Boolean object) {
					if (object) {
						editContext.setPropertyValue(editContext.instantiate(editContext.getPropertyGetter().getReturnType()));
					} else {
						editContext.setPropertyValue(null);
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					Component valueEditor = newValueEditor();
					replace(valueEditor);
					target.add(valueEditor);
				}
				
			}));
			
		}

		add(newValueEditor());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

	private Component newValueEditor() {
		EditContext valueContext = editContext.getValueContext();
		Component valueEditor;
		if (valueContext != null) {
			valueEditor = (Component)valueContext.renderForEdit(VALUE_EDITOR_ID);
		} else {
			valueEditor = new WebMarkupContainer(VALUE_EDITOR_ID).setVisible(false);
		}
		valueEditor.setOutputMarkupId(true);
		valueEditor.setOutputMarkupPlaceholderTag(true);
		return valueEditor;
	}

}

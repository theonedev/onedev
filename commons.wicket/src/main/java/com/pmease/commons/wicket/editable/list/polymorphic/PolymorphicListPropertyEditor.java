package com.pmease.commons.wicket.editable.list.polymorphic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.editable.BeanEditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.commons.wicket.asset.CommonHeaderItem;

@SuppressWarnings("serial")
public class PolymorphicListPropertyEditor extends Panel {

	private static final String ELEMENT_EDITOR_ID = "elementEditor";

	private final PolymorphicListPropertyEditConext editContext;
	
	public PolymorphicListPropertyEditor(String id, PolymorphicListPropertyEditConext editContext) {
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
						editContext.setPropertyValue(editContext.instantiate(ArrayList.class));
					} else {
						editContext.setPropertyValue(null);
					}
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick"){

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					Component editor = newPropertyValueEditor();
					replace(editor);
					target.add(editor);
				}
				
			}));
			
		}
		
		add(newPropertyValueEditor());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CommonHeaderItem.get());
	}

	private Component newPropertyValueEditor() {
		final WebMarkupContainer table = new WebMarkupContainer("valueEditor") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editContext.getElementContexts() != null);
			}
			
		};

		table.setOutputMarkupId(true);
		table.setOutputMarkupPlaceholderTag(true);
		
		table.add(new ListView<BeanEditContext>("elements", editContext.getElementContexts()) {

			@Override
			protected void populateItem(final ListItem<BeanEditContext> item) {
				List<String> implementationNames = new ArrayList<String>();
				for (Class<?> each: editContext.getImplementations())
					implementationNames.add(EditableUtils.getName(each));
						
				item.add(new DropDownChoice<String>("elementTypeSelector", new IModel<String>() {
					
					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return EditableUtils.getName(item.getModelObject().getBeanClass());
					}

					@Override
					public void setObject(String object) {
						for (Class<?> each: editContext.getImplementations()) {
							if (EditableUtils.getName(each).equals(object)) {
								Serializable newElementValue = editContext.instantiate(each);
								editContext.removeElement(item.getIndex());
								editContext.addElement(item.getIndex(), newElementValue);
								item.setModelObject(editContext.getElementContexts().get(item.getIndex()));
								return;
							}
						}
						
						throw new IllegalStateException("Unable to find implementation with name: " + object);
					}
					
				}, implementationNames).setNullValid(false).add(new AjaxFormComponentUpdatingBehavior("onclick"){

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						Component elementEditor = newElementEditor(item.getModelObject());
						item.replace(elementEditor);
						target.add(elementEditor);
					}
					
				}));

				item.add(new AjaxButton("deleteElement") {

					@Override
					protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
						super.onSubmit(target, form);

						WicketUtils.updateFormModels(form);
						editContext.removeElement(item.getIndex());
						target.add(table);
					}

				}.setDefaultFormProcessing(false));

				item.add(new ListView<String>("elementValidationErrors", item.getModelObject().getValidationErrors()) {

					@Override
					protected void populateItem(ListItem<String> item) {
						item.add(new Label("propertyValidationError", item.getModelObject()));
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						
						setVisible(!item.getModelObject().getValidationErrors().isEmpty());
					}
					
				});

				item.add(newElementEditor(item.getModelObject()));
			}
			
		});
		
		table.add(new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(editContext.getElementContexts().isEmpty());
			}
			
		});
		
		WebMarkupContainer newRow = new WebMarkupContainer("addElement");
		newRow.add(new AjaxButton("addElement") {

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				WicketUtils.updateFormModels(form);
				editContext.addElement(editContext.getElementContexts().size(), 
						editContext.instantiate(editContext.getImplementations().get(0)));
				target.add(table);
			}
			
		}.setDefaultFormProcessing(false));
		
		table.add(newRow);
		
		return table;		
	}

	private Component newElementEditor(BeanEditContext elementContext) {
		Component elementEditor = (Component)elementContext.renderForEdit(ELEMENT_EDITOR_ID);
		elementEditor.setOutputMarkupId(true);
		elementEditor.setOutputMarkupPlaceholderTag(true);
		return elementEditor;
	}

}

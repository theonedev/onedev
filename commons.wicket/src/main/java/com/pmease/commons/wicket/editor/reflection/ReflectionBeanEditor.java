package com.pmease.commons.wicket.editor.reflection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.pmease.commons.editable.BeanDescriptor;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.commons.editable.annotation.TableLayout;
import com.pmease.commons.wicket.editor.BeanEditor;
import com.pmease.commons.wicket.editor.ErrorContext;
import com.pmease.commons.wicket.editor.PathSegment;
import com.pmease.commons.wicket.editor.PathSegment.Property;
import com.pmease.commons.wicket.editor.PropertyEditContext;
import com.pmease.commons.wicket.editor.PropertyEditor;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class ReflectionBeanEditor extends BeanEditor<Serializable> {

	private final List<PropertyEditContext<Serializable>> propertyContexts = new ArrayList<>();

	public ReflectionBeanEditor(String id, BeanDescriptor<Serializable> beanDescriptor, IModel<Serializable> model) {
		super(id, beanDescriptor, model);

		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			propertyContexts.add(PropertyEditContext.of(propertyDescriptor));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		Fragment fragment;
		if (getBeanDescriptor().getBeanClass().getAnnotation(TableLayout.class) == null)
			fragment = new Fragment("content", "default", ReflectionBeanEditor.this);
		else
			fragment = new Fragment("content", "table", ReflectionBeanEditor.this);
		
		add(fragment);
		
		fragment.add(new ListView<PropertyEditContext<Serializable>>("properties", propertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyEditContext<Serializable>> item) {
				PropertyEditContext<Serializable> propertyContext = item.getModelObject();
				
				Label nameLabel = new Label("name", EditableUtils.getName(propertyContext.getPropertyGetter()));
				item.add(nameLabel);
				
				OmitName omitName = propertyContext.getPropertyGetter().getAnnotation(OmitName.class);
				if (omitName != null && omitName.value() != OmitName.Place.VIEWER)
					nameLabel.setVisible(false);

				String required;
				if (propertyContext.isPropertyRequired() && propertyContext.getPropertyClass() != boolean.class)
					required = "*";
				else
					required = "&nbsp;";
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
				
				Serializable propertyValue = propertyContext.getPropertyValue(ReflectionBeanEditor.this.getModelObject());
				PropertyEditor<Serializable> propertyEditor = propertyContext.renderForEdit("value", Model.of(propertyValue)); 
				item.add(propertyEditor);
				
				String description = EditableUtils.getDescription(propertyContext.getPropertyGetter());
				if (description != null)
					item.add(new Label("description", description).setEscapeModelStrings(false));
				else
					item.add(new Label("description").setVisible(false));
				
				item.add(new NotificationPanel("feedback", propertyEditor));
			}

		});
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		final PathSegment.Property property = (Property) pathSegment;
		return visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (object.getPropertyDescriptor().getPropertyName().equals(property.getname()))
					visit.stop(object);
				else
					visit.dontGoDeeper();
			}
			
		});
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		final Serializable bean = getBeanDescriptor().newBeanInstance();
		
		visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				object.getPropertyDescriptor().setPropertyValue(bean, object.getConvertedInput());
				visit.dontGoDeeper();
			}
			
		});
		
		return bean;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(OnDomReadyHeaderItem.forScript(String.format("pmease.commons.editable.adjustReflectionEditor('%s')", getMarkupId())));
	}
}

package com.pmease.commons.wicket.editable.reflection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.pmease.commons.wicket.editable.BeanDescriptor;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.EditableUtils;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PathSegment.Property;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.annotation.Horizontal;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.commons.wicket.editable.annotation.Vertical;

@SuppressWarnings("serial")
public class ReflectionBeanEditor extends BeanEditor<Serializable> {

	private final List<PropertyContext<Serializable>> propertyContexts = new ArrayList<>();
	
	private final boolean vertical;
	
	public ReflectionBeanEditor(String id, BeanDescriptor beanDescriptor, IModel<Serializable> model) {
		super(id, beanDescriptor, model);

		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			propertyContexts.add(PropertyContext.of(propertyDescriptor));
		}
		
		Class<?> beanClass = beanDescriptor.getBeanClass();
		if (beanClass.getAnnotation(Vertical.class) != null)
			vertical = true;
		else if (beanClass.getAnnotation(Horizontal.class) != null)
			vertical = false;
		else 
			vertical = true;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		Fragment fragment;
		if (vertical) {
			fragment = new Fragment("content", "verticalFrag", ReflectionBeanEditor.this);
			fragment.add(AttributeAppender.append("class", " vertical"));
		} else {
			fragment = new Fragment("content", "horizontalFrag", ReflectionBeanEditor.this);
			fragment.add(AttributeAppender.append("class", " horizontal"));
		}
		
		add(fragment);
		
		RepeatingView propertiesView = new RepeatingView("properties");
		fragment.add(propertiesView);
		
		for (PropertyContext<Serializable> propertyContext: propertyContexts) {
			WebMarkupContainer item = new WebMarkupContainer(propertiesView.newChildId());
			propertiesView.add(item);
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
			
			Serializable propertyValue = (Serializable) propertyContext.getPropertyValue(ReflectionBeanEditor.this.getModelObject());
			PropertyEditor<Serializable> propertyEditor = propertyContext.renderForEdit("value", Model.of(propertyValue)); 
			item.add(propertyEditor);
			
			String description = EditableUtils.getDescription(propertyContext.getPropertyGetter());
			if (description != null)
				item.add(new Label("description", description).setEscapeModelStrings(false));
			else
				item.add(new Label("description").setVisible(false));
			
			item.add(new FencedFeedbackPanel("feedback", propertyEditor));
		}
		
	}

	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		final PathSegment.Property property = (Property) pathSegment;
		return visitChildren(PropertyEditor.class, new IVisitor<PropertyEditor<Serializable>, PropertyEditor<Serializable>>() {

			@Override
			public void component(PropertyEditor<Serializable> object, IVisit<PropertyEditor<Serializable>> visit) {
				if (object.getPropertyDescriptor().getPropertyName().equals(property.getName()))
					visit.stop(object);
				else
					visit.dontGoDeeper();
			}
			
		});
	}

	@Override
	protected Serializable convertInputToValue() throws ConversionException {
		final Serializable bean = (Serializable) getBeanDescriptor().newBeanInstance();
		
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

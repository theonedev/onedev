package com.pmease.commons.wicket.editable.list.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import com.pmease.commons.editable.BeanDescriptorImpl;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.wicket.behavior.sortable.SortPosition;
import com.pmease.commons.wicket.behavior.sortable.SortBehavior;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.commons.wicket.editable.PropertyEditor;
import com.pmease.commons.wicket.editable.PathSegment.Property;

@SuppressWarnings("serial")
public class TableListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final List<PropertyContext<Serializable>> propertyContexts;
	
	private final Class<?> elementClass;
	
	public TableListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		elementClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());

		propertyContexts = new ArrayList<>();
		
		for (PropertyDescriptor each: new BeanDescriptorImpl(elementClass).getPropertyDescriptors()) {
			propertyContexts.add(PropertyContext.of(each));
		}

	}
	
	@SuppressWarnings("unchecked")
	private List<Serializable> newList() {
		if (ClassUtils.isConcrete(getPropertyDescriptor().getPropertyClass())) {
			try {
				return (List<Serializable>) getPropertyDescriptor().getPropertyClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new ArrayList<Serializable>();
		}
	}
	
	private Serializable newElement() {
		try {
			return (Serializable) elementClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getPropertyDescriptor().isPropertyRequired()) {
			add(new WebMarkupContainer("enable").setVisible(false));
		} else {
			add(new CheckBox("enable", new IModel<Boolean>() {
				
				@Override
				public void detach() {
					
				}
	
				@Override
				public Boolean getObject() {
					return TableListPropertyEditor.this.get("listEditor").isVisible();
				}
	
				@Override
				public void setObject(Boolean object) {
					TableListPropertyEditor.this.get("listEditor").setVisible(object);
				}
				
			}).add(new AjaxFormComponentUpdatingBehavior("onclick") {
				
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					target.add(TableListPropertyEditor.this.get("listEditor"));
				}
				
			}));
			
		}
		
		List<Serializable> list = getModelObject();
		if (list == null && getPropertyDescriptor().isPropertyRequired())
			list = newList(); 
		add(newListEditor(list));
	}

	private Component newListEditor(List<Serializable> list) {
		WebMarkupContainer table = new WebMarkupContainer("listEditor");

		table.setOutputMarkupId(true);
		table.setOutputMarkupPlaceholderTag(true);
		
		table.add(new ListView<PropertyContext<?>>("headers", propertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyContext<?>> item) {
				item.add(new Label("header", EditableUtils.getName(item.getModelObject().getPropertyGetter())));
				
				String required;
				if (item.getModelObject().isPropertyRequired() && item.getModelObject().getPropertyClass() != boolean.class)
					required = "*";
				else
					required = "&nbsp;";
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
			}
			
		});
		
		final RepeatingView rows = new RepeatingView("elements");
		table.add(rows);
		
		if (list != null) {
			for (Serializable element: list) {
				addRow(rows, element);
			}
		} else {
			table.setVisible(false);
		}
		
		WebMarkupContainer noElements = new WebMarkupContainer("noElements") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!rows.iterator().hasNext());
			}
			
		};
		
		noElements.add(AttributeModifier.append("colspan", propertyContexts.size() + 1));
		table.add(noElements);
		
		WebMarkupContainer newRow = new WebMarkupContainer("newRow");
		newRow.add(AttributeModifier.append("colspan", propertyContexts.size() + 1));
		newRow.add(new AjaxButton("addElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				addRow(rows, newElement());
				target.add(TableListPropertyEditor.this.get("listEditor"));
			}

		}.setDefaultFormProcessing(false));
		
		table.add(newRow);
		
		table.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<Component> children = new ArrayList<>();
				for (Component child: rows)
					children.add(child);

				Component fromChild = children.remove(from.getItemIndex());
				children.add(to.getItemIndex(), fromChild);
				
				rows.removeAll();
				for (Component child: children)
					rows.add(child);
			}
			
		}.sortable("tbody").handle(".handle").helperClass("sort-helper"));

		return table;		
	}

	private void addRow(final RepeatingView rows, Serializable element) {
		final WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		rows.add(row);
		
		RepeatingView columns = new RepeatingView("properties");
		row.add(columns);
		
		for (PropertyContext<Serializable> propertyContext: propertyContexts) {
			WebMarkupContainer column = new WebMarkupContainer(columns.newChildId());
			columns.add(column);
			
			Serializable propertyValue = (Serializable) propertyContext.getPropertyValue(element);
			PropertyEditor<?> propertyEditor = propertyContext.renderForEdit("propertyEditor", Model.of(propertyValue));
			column.add(propertyEditor);
			column.add(new FencedFeedbackPanel("feedback", propertyEditor));
		}
		
		row.add(new AjaxButton("deleteElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				rows.remove(row);
				target.add(TableListPropertyEditor.this.get("listEditor"));
			}

		}.setDefaultFormProcessing(false));
	}
	
	@Override
	public ErrorContext getErrorContext(PathSegment pathSegment) {
		final int index = ((PathSegment.Element) pathSegment).getIndex();
		final String messagePrefix = "Item " + (index+1) + ": ";
		
		return new ErrorContext() {

			@Override
			public void addError(String errorMessage) {
				error(messagePrefix + errorMessage);
			}

			@Override
			public boolean hasErrors() {
				boolean found = false;
				for (FeedbackMessage message: getFeedbackMessages()) {
					if (message.getMessage().toString().startsWith(messagePrefix)) {
						found = true;
						break;
					}
				}
				return found;
			}

			@Override
			public ErrorContext getErrorContext(PathSegment pathSegment) {
				PathSegment.Property property = (Property) pathSegment;

				WebMarkupContainer table = (WebMarkupContainer) get("listEditor");
				RepeatingView rows = (RepeatingView) table.get("elements");

				int currentIndex = 0;
				Iterator<Component> it = rows.iterator();
				Component row = it.next();
				while (currentIndex++ < index) {
					row = it.next();
				}
				
				RepeatingView columns = (RepeatingView) row.get("properties");
				for (Component column: columns) {
					@SuppressWarnings("unchecked")
					PropertyEditor<Serializable> propertyEditor = (PropertyEditor<Serializable>) column.get("propertyEditor");
					if (propertyEditor.getPropertyDescriptor().getPropertyName().equals(property.getName()))
						return propertyEditor;
				}
				return null;
			}
			
		};
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		if (get("listEditor").isVisible()) {
			List<Serializable> newList = newList();
			RepeatingView rows = (RepeatingView) get("listEditor").get("elements");
			for (Component row: rows) {
				Serializable element = newElement();
				newList.add(element);
				
				RepeatingView columns = (RepeatingView) row.get("properties");
				for (Component column: columns) {
					@SuppressWarnings("unchecked")
					PropertyEditor<Serializable> propertyEditor = (PropertyEditor<Serializable>) column.get("propertyEditor");

					propertyEditor.getPropertyDescriptor().setPropertyValue(element, propertyEditor.getConvertedInput());
				}
			}
			return newList;
		} else {
			return null;
		}
	}
}

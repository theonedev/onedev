package io.onedev.server.web.editable.beanlist;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.ErrorContext;
import io.onedev.server.web.editable.PathSegment;
import io.onedev.server.web.editable.PathSegment.Property;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.utils.ClassUtils;

@SuppressWarnings("serial")
public class BeanListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final List<PropertyContext<Serializable>> propertyContexts;
	
	private final Class<?> elementClass;
	
	public BeanListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		elementClass = EditableUtils.getElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());

		propertyContexts = new ArrayList<>();
		
		for (PropertyDescriptor each: new BeanDescriptor(elementClass).getPropertyDescriptors()) {
			propertyContexts.add(PropertyContext.of(each));
		}

	}
	
	@SuppressWarnings("unchecked")
	private List<Serializable> newList() {
		if (ClassUtils.isConcrete(getDescriptor().getPropertyClass())) {
			try {
				return (List<Serializable>) getDescriptor().getPropertyClass().newInstance();
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
		
		List<Serializable> list = getModelObject();
		if (list == null && getDescriptor().isPropertyRequired())
			list = newList(); 
		add(newListEditor(list));
	}

	@Override
	protected String getErrorClass() {
		return null;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating)event.getPayload()).getHandler());
		}		
	}
	
	private Component newListEditor(List<Serializable> list) {
		WebMarkupContainer listEditor = new WebMarkupContainer("listEditor");

		listEditor.setOutputMarkupPlaceholderTag(true);
		
		listEditor.add(new ListView<PropertyContext<Serializable>>("headers", propertyContexts) {

			@Override
			protected void populateItem(ListItem<PropertyContext<Serializable>> item) {
				PropertyContext<Serializable> propertyContext = item.getModelObject();
				item.add(new Label("header", EditableUtils.getDisplayName(propertyContext.getPropertyGetter())));
				item.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
				
				String required;
				if (propertyContext.isPropertyRequired() && propertyContext.getPropertyClass() != boolean.class)
					required = "*";
				else
					required = "&nbsp;";
				
				item.add(new Label("required", required).setEscapeModelStrings(false));
				String description = EditableUtils.getDescription(propertyContext.getPropertyGetter());
				if (description != null) {
					WebMarkupContainer help = new WebMarkupContainer("help");
					help.add(AttributeAppender.append("title", description));
					item.add(help);
				} else {
					item.add(new WebMarkupContainer("help").setVisible(false));
				}
			}
			
		});
		
		RepeatingView rows = new RepeatingView("elements");
		listEditor.add(rows);
		
		if (list != null) {
			for (Serializable element: list) {
				addRow(rows, element);
			}
		} else {
			listEditor.setVisible(false);
		}
		
		WebMarkupContainer newRow = new WebMarkupContainer("newRow");
		newRow.add(AttributeModifier.append("colspan", propertyContexts.size() + 1));
		newRow.add(new AjaxButton("addElement") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", "Add " + EditableUtils.getDisplayName(elementClass)));
			}

			@SuppressWarnings("deprecation")
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				Component lastRow;
				if (rows.size() != 0)
					lastRow = rows.get(rows.size() - 1);
				else 
					lastRow = null;
				
				Component newRow = addRow(rows, newElement());
				String script = String.format("$('<tr id=\"%s\"></tr>')", newRow.getMarkupId());
				if (lastRow != null)
					script += ".insertAfter('#" + lastRow.getMarkupId() + "');";
				else
					script += ".appendTo('#" + listEditor.getMarkupId() + ">tbody');";

				target.prependJavaScript(script);
				target.add(newRow);
				
				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));
		
		listEditor.add(newRow);
		
		listEditor.add(new SortBehavior() {

			@SuppressWarnings("deprecation")
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
 				/*
				List<Component> children = new ArrayList<>();
				for (Component child: rows)
					children.add(child);

				Component fromChild = children.remove(from.getItemIndex());
				children.add(to.getItemIndex(), fromChild);
				
				rows.removeAll();
				for (Component child: children)
					rows.add(child);
				*/
				
				// Do not use code above as removing components outside of a container and add again 
				// can cause the fenced feedback panel not functioning properly
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						rows.swap(fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						rows.swap(fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
			}
			
		}.sortable("tbody").helperClass("sort-helper"));

		return listEditor;		
	}

	private WebMarkupContainer addRow(RepeatingView rows, Serializable element) {
		WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		row.setOutputMarkupId(true);
		rows.add(row);
		
		RepeatingView columns = new RepeatingView("properties");
		row.add(columns);
		
		for (PropertyContext<Serializable> propertyContext: propertyContexts) {
			WebMarkupContainer column = new WebMarkupContainer(columns.newChildId());
			column.add(AttributeAppender.append("class", "property-" + propertyContext.getPropertyName()));
			columns.add(column);
			
			Serializable propertyValue = (Serializable) propertyContext.getPropertyValue(element);
			PropertyEditor<?> propertyEditor = propertyContext.renderForEdit("propertyEditor", Model.of(propertyValue));
			column.add(propertyEditor);
			column.add(new FencedFeedbackPanel("feedback", propertyEditor));
		}
		
		row.add(new AjaxButton("deleteElement") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.replace("title", "Delete this " + EditableUtils.getDisplayName(elementClass).toLowerCase()));
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				target.appendJavaScript($(row).chain("remove").get());
				target.appendJavaScript(String.format("onedev.server.form.markDirty($('#%s'));", form.getMarkupId(true)));
				rows.remove(row);
				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));
		
		return row;
	}
	
	@SuppressWarnings("unchecked")
	private List<PropertyEditor<Serializable>> getPropertyEditorsAtRow(int index) {
		WebMarkupContainer table = (WebMarkupContainer) get("listEditor");
		RepeatingView rows = (RepeatingView) table.get("elements");

		int currentIndex = 0;
		Iterator<Component> it = rows.iterator();
		Component row = it.next();
		while (currentIndex++ < index) {
			row = it.next();
		}
		
		List<PropertyEditor<Serializable>> propertyEditors = new ArrayList<>();
		RepeatingView columns = (RepeatingView) row.get("properties");
		for (Component column: columns) {
			propertyEditors.add((PropertyEditor<Serializable>) column.get("propertyEditor"));
		}
		
		return propertyEditors;
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
			public boolean hasErrors(boolean recursive) {
				for (FeedbackMessage message: getFeedbackMessages()) {
					if (message.getMessage().toString().startsWith(messagePrefix)) {
						return true;
					}
				}
				
				if (recursive) {
					for (PropertyEditor<Serializable> propertyEditor: getPropertyEditorsAtRow(index)) {
						if (propertyEditor.hasErrors(true))
							return true;
					}
				} 
				return false;
			}

			@Override
			public ErrorContext getErrorContext(PathSegment pathSegment) {
				PathSegment.Property property = (Property) pathSegment;

				for (PropertyEditor<Serializable> propertyEditor: getPropertyEditorsAtRow(index)) {
					if (propertyEditor.getDescriptor().getPropertyName().equals(property.getName()))
						return propertyEditor;
				}
				return null;
			}
			
		};
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> newList = newList();
		
		RepeatingView rows = (RepeatingView) get("listEditor").get("elements");
		for (Component row: rows) {
			Serializable element = newElement();
			newList.add(element);
			
			RepeatingView columns = (RepeatingView) row.get("properties");
			for (Component column: columns) {
				@SuppressWarnings("unchecked")
				PropertyEditor<Serializable> propertyEditor = (PropertyEditor<Serializable>) column.get("propertyEditor");
				propertyEditor.getDescriptor().setPropertyValue(element, propertyEditor.getConvertedInput());
			}
		}
		return newList;
	}
	
}

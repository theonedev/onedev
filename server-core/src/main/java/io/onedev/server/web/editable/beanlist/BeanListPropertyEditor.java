package io.onedev.server.web.editable.beanlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Indexed;
import io.onedev.server.util.PathNode.Named;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.server.web.editable.annotation.ExcludedProperties;

@SuppressWarnings("serial")
public class BeanListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final List<PropertyContext<Serializable>> propertyContexts;
	
	private final Class<?> elementClass;
	
	private RepeatingView rows;
	
	private WebMarkupContainer noRecords;
	
	public BeanListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		elementClass = ReflectionUtils.getCollectionElementType(propertyDescriptor.getPropertyGetter().getGenericReturnType());

		propertyContexts = new ArrayList<>();
		
		Set<String> excludedProperties = new HashSet<>();
		ExcludedProperties excludedPropertiesAnnotation = 
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) {
			for (String each: excludedPropertiesAnnotation.value())
				excludedProperties.add(each);
		}
		for (List<PropertyDescriptor> groupProperties: new BeanDescriptor(elementClass).getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (!excludedProperties.contains(property.getPropertyName()))
					propertyContexts.add(PropertyContext.of(property));
			}
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
		if (list == null)
			list = newList(); 
		
		WebMarkupContainer table = new WebMarkupContainer("table") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				if (rows.size() == 0) 
					NoRecordsBehavior.decorate(tag);
			}
			
		};
		add(table);
		
		table.add(new ListView<PropertyContext<Serializable>>("headers", propertyContexts) {

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
		
		rows = new RepeatingView("elements");
		table.add(rows);
		
		for (Serializable element: list)
			addRow(element);
		
		add(new AjaxButton("addElement") {

			@SuppressWarnings("deprecation")
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				markFormDirty(target);
				
				Component lastRow;
				if (rows.size() != 0)
					lastRow = rows.get(rows.size() - 1);
				else 
					lastRow = null;
				
				Component newRow = addRow(newElement());
				String script = String.format("$('<tr id=\"%s\"></tr>')", newRow.getMarkupId());
				if (lastRow != null)
					script += ".insertAfter('#" + lastRow.getMarkupId() + "');";
				else
					script += ".appendTo('#" + BeanListPropertyEditor.this.getMarkupId() + ">div>table>tbody');";
				
				target.prependJavaScript(script);
				target.add(newRow);
				target.add(noRecords);
				if (rows.size() == 1) {
					target.appendJavaScript(String.format("$('#%s>div>table').removeClass('%s');", 
							BeanListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
				}

				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));
		
		table.add(noRecords = new WebMarkupContainer("noRecords") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new WebMarkupContainer("td").add(AttributeAppender.append("colspan", propertyContexts.size()+1)));
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(rows.size() == 0);
			}
			
		});
		
		add(new SortBehavior() {

			@SuppressWarnings("deprecation")
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				markFormDirty(target);
				
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
			
		}.sortable("tbody"));	
		
		setOutputMarkupId(true);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating)event.getPayload()).getHandler());
		}		
	}
	
	private WebMarkupContainer addRow(Serializable element) {
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
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				markFormDirty(target);
				target.appendJavaScript(String.format("$('#%s').remove();", row.getMarkupId()));
				rows.remove(row);
				target.add(noRecords);

				if (rows.size() == 0) {
					target.appendJavaScript(String.format("$('#%s>div>table').addClass('%s');", 
							BeanListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
				}
				
				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));
		
		return row;
	}
	
	@SuppressWarnings("unchecked")
	private List<PropertyEditor<Serializable>> getPropertyEditorsAtRow(int index) {
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
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		int index = ((Indexed) propertyNode).getIndex();
		String messagePrefix = "Item " + (index+1) + ": ";
		PathNode.Named named = (Named) pathInProperty.takeNode();
		if (named != null) {
			for (PropertyEditor<Serializable> propertyEditor: getPropertyEditorsAtRow(index)) {
				if (propertyEditor.getDescriptor().getPropertyName().equals(named.getName())) {
					error(pathInProperty, errorMessage);
					break;
				}
			}
		} else {
			error(messagePrefix + errorMessage);
		}
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> newList = newList();
		
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

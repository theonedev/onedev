package io.onedev.server.web.editable.polymorphiclist;

import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.annotation.ExcludedProperties;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.polymorphiceditor.PolymorphicEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static java.util.Arrays.asList;

public class PolymorphicListPropertyEditor extends PropertyEditor<List<Serializable>> {

	private final Class<? extends Serializable> baseClass;
	
	private RepeatingView rows;

	private WebMarkupContainer noRecords;
	
	private Set<String> excludedProperties = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	public PolymorphicListPropertyEditor(String id, PropertyDescriptor propertyDescriptor, 
										 IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);

		baseClass = (Class<? extends Serializable>) ReflectionUtils.getCollectionElementClass(propertyDescriptor.getPropertyGetter().getGenericReturnType());
		
		ExcludedProperties excludedPropertiesAnnotation =
				descriptor.getPropertyGetter().getAnnotation(ExcludedProperties.class);
		if (excludedPropertiesAnnotation != null) 
			excludedProperties.addAll(asList(excludedPropertiesAnnotation.value()));
	}

	@SuppressWarnings("unchecked")
	private List<Serializable> newList() {
		if (ClassUtils.isConcrete(getDescriptor().getPropertyClass())) {
			try {
				return (List<Serializable>) getDescriptor().getPropertyClass().getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		} else {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("deprecation")
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
		
		rows = new RepeatingView("elements");
		table.add(rows);

		for (Serializable element: list)
			addRow(element);

		add(new AjaxButton("addElement") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				markFormDirty(target);

				Component lastRow;
				if (rows.size() != 0)
					lastRow = rows.get(rows.size() - 1);
				else
					lastRow = null;

				Component newRow = addRow(null);
				String script = String.format("$('<tr id=\"%s\"></tr>')", newRow.getMarkupId());
				if (lastRow != null)
					script += ".insertAfter('#" + lastRow.getMarkupId() + "');";
				else
					script += ".appendTo('#" + PolymorphicListPropertyEditor.this.getMarkupId() + ">div>table>tbody');";

				target.prependJavaScript(script);
				target.add(newRow);
				target.add(noRecords);
				if (rows.size() == 1) {
					target.appendJavaScript(String.format("$('#%s>div>table').removeClass('%s');",
							PolymorphicListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
				}

				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));

		table.add(noRecords = new WebMarkupContainer("noRecords") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(rows.size() == 0);
			}

		});
		noRecords.setOutputMarkupPlaceholderTag(true);

		add(new SortBehavior() {

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
		
		add(validatable -> {
			var index = 0;
			for (var element: validatable.getValue()) {
				if (element == null)
					rows.get(index).error("must not be null");
				index++;
			}
		});
		
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

	private WebMarkupContainer addRow(@Nullable Serializable element) {
		WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		row.setOutputMarkupId(true);
		rows.add(row);
		
		row.add(new PolymorphicEditor("elementEditor", baseClass, Model.of(element)) {

			@Override
			protected boolean isNullValid() {
				return false;
			}

			@Override
			protected Set<String> getExcludedProperties() {
				return excludedProperties;
			}

			@Override
			protected void onTypeChanging(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}
			
		});
		row.add(new FencedFeedbackPanel("feedback", row));
		
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
							PolymorphicListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
				}

				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));

		return row;
	}

	private PolymorphicEditor getElementEditorAtRow(int index) {
		int currentIndex = 0;
		Iterator<Component> it = rows.iterator();
		Component row = it.next();
		while (currentIndex++ < index) {
			row = it.next();
		}
		return (PolymorphicEditor) row.get("elementEditor");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		int index = ((PathNode.Indexed) propertyNode).getIndex();
		PathNode.Named named = (PathNode.Named) pathInProperty.takeNode();
		if (named != null) 
			getElementEditorAtRow(index).error(named, pathInProperty, errorMessage);
		else 
			rows.get(index).error(errorMessage);
	}

	@Override
	protected String getInvalidClass() {
		return null;
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> newList = newList();

		for (Component row: rows) {
			PolymorphicEditor elementEditor = (PolymorphicEditor) row.get("elementEditor");
			newList.add(elementEditor.getConvertedInput());			
		}
		return newList;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}

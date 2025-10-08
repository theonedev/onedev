package io.onedev.server.web.editable.interpolativestringlist;

import com.google.common.collect.Lists;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.web.behavior.InterpolativeAssistBehavior;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class InterpolativeStringListPropertyEditor extends PropertyEditor<List<String>> {
	
	private RepeatingView rows;

	private WebMarkupContainer noRecords;
	
	public InterpolativeStringListPropertyEditor(String id, PropertyDescriptor propertyDescriptor,
												 IModel<List<String>> model) {
		super(id, propertyDescriptor, model);
	}

	@SuppressWarnings("unchecked")
	private List<String> newList() {
		if (ClassUtils.isConcrete(getDescriptor().getPropertyClass())) {
			try {
				return (List<String>) getDescriptor().getPropertyClass().getDeclaredConstructor().newInstance();
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

		List<String> list = getModelObject();
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
			addRow((String) element);

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
					script += ".appendTo('#" + InterpolativeStringListPropertyEditor.this.getMarkupId() + ">div>table>tbody');";

				target.prependJavaScript(script);
				target.add(newRow);
				target.add(noRecords);
				if (rows.size() == 1) {
					target.appendJavaScript(String.format("$('#%s>div>table').removeClass('%s');",
							InterpolativeStringListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
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

	private WebMarkupContainer addRow(@Nullable String element) {
		WebMarkupContainer row = new WebMarkupContainer(rows.newChildId());
		row.setOutputMarkupId(true);
		rows.add(row);

		TextField<String> input;
		row.add(input = new TextField<>("elementEditor", Model.of(element)));
		input.setType(String.class);

		var interpolative = descriptor.getPropertyGetter().getAnnotation(Interpolative.class);
		InterpolativeAssistBehavior inputAssist = new InterpolativeAssistBehavior() {

			@SuppressWarnings("unchecked")
			@Override
			protected List<InputSuggestion> suggestVariables(String matchWith) {
				String suggestionMethod = interpolative.variableSuggester();
				if (suggestionMethod.length() != 0) {
					return (List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
							descriptor.getBeanClass(), suggestionMethod, new Object[] {matchWith});
				} else {
					return Lists.newArrayList();
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			protected List<InputSuggestion> suggestLiterals(String matchWith) {
				String suggestionMethod = interpolative.literalSuggester();
				if (suggestionMethod.length() != 0) {
					return (List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
							descriptor.getBeanClass(), suggestionMethod, new Object[] {matchWith});
				} else {
					return Lists.newArrayList();
				}
			}

		};
		
		input.add(inputAssist);
		input.add(AttributeAppender.append("spellcheck", "false"));
		input.add(AttributeAppender.append("autocomplete", "off"));

		input.add(new OnTypingDoneBehavior() {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				onPropertyUpdating(target);
			}

		});
		input.add(newPlaceholderModifier());		
		
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
							InterpolativeStringListPropertyEditor.this.getMarkupId(), NoRecordsBehavior.CSS_CLASS));
				}

				onPropertyUpdating(target);
			}

		}.setDefaultFormProcessing(false));

		return row;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		int index = ((PathNode.Indexed) propertyNode).getIndex();
		rows.get(index).error(errorMessage);
	}

	@Override
	protected String getInvalidClass() {
		return null;
	}

	@Override
	protected List<String> convertInputToValue() throws ConversionException {
		List<String> newList = newList();

		for (Component row: rows) {
			@SuppressWarnings("unchecked")
			TextField<String> elementEditor = (TextField<String>) row.get("elementEditor");
			newList.add(elementEditor.getConvertedInput());			
		}
		return newList;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}

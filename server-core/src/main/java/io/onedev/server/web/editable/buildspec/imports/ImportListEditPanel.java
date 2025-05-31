package io.onedev.server.web.editable.buildspec.imports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.Import;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Indexed;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

class ImportListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<Import> imports;
	
	private RepeatingView importsView;
	
	public ImportListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		imports = new ArrayList<>();
		for (Serializable each: model.getObject())
			imports.add((Import) each);
	}
	
	@Override
	protected String getInvalidClass() {
		return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		importsView = new RepeatingView("imports");
		for (Import aImport: imports) 
			importsView.add(newImportEditor(importsView.newChildId(), aImport));
		add(importsView);
		
		add(new AjaxLink<Void>("add") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				Component importEditor = newImportEditor(importsView.newChildId(), new Import());
				importsView.add(importEditor);
				target.add(importEditor);
				
				String script = String.format(""
						+ "$('#%s').before('<div id=\"%s\"/>');",
						getMarkupId(), importEditor.getMarkupId());
				target.prependJavaScript(script);
			}
			
		});
		
		add(new SortBehavior() {
			
			@SuppressWarnings("deprecation")
			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++)  
						importsView.swap(fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++)
						importsView.swap(fromIndex-i, fromIndex-i-1);
				}
			}
			
		}.sortable(">.imports").items(".import").handle(".import-head"));
		
	}
	
	protected Component newImportEditor(String componentId, Import aImport) {
		Fragment fragment = new Fragment(componentId, "importEditFrag", this);
		fragment.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				importsView.remove(fragment);
				
				String script = String.format("$('#%s').remove();", fragment.getMarkupId());
				target.appendJavaScript(script);
			}
			
		});
		
		fragment.add(new FencedFeedbackPanel("feedback", fragment));
		fragment.add(BeanContext.edit("editor", aImport));
		fragment.setOutputMarkupId(true);
		
		return fragment;
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating)event.getPayload()).getHandler());
		}		
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component each: importsView) {
			BeanEditor editor = (BeanEditor) each.get("editor");
			value.add(editor.getConvertedInput());
		}
		return value;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		int index = ((Indexed) propertyNode).getIndex();
		BeanEditor editor = (BeanEditor) importsView.get(index).get("editor");
		editor.error(pathInProperty, errorMessage);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ImportCssResourceReference()));
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}
	
}

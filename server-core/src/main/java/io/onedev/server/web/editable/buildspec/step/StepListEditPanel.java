package io.onedev.server.web.editable.buildspec.step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.util.PathNode.Indexed;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class StepListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<StepEditBean> beans;
	
	private RepeatingView stepsView;
	
	public StepListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		beans = new ArrayList<>();
		for (Serializable each: model.getObject()) { 
			StepEditBean bean = new StepEditBean();
			bean.setStep((Step) each);
			beans.add(bean);
		}
	}
	
	@Override
	protected String getInvalidClass() {
		return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		stepsView = new RepeatingView("steps");
		for (StepEditBean bean: beans) 
			stepsView.add(newStepEditor(stepsView.newChildId(), bean));
		add(stepsView);
		
		add(new AjaxLink<Void>("add") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				StepEditBean bean = new StepEditBean();
				bean.setStep(new CommandStep());
				Component stepEditor = newStepEditor(stepsView.newChildId(), bean);
				stepsView.add(stepEditor);
				target.add(stepEditor);
				
				String script = String.format(""
						+ "$('#%s').before('<div id=\"%s\"/>');",
						getMarkupId(), stepEditor.getMarkupId());
				target.prependJavaScript(script);
				target.appendJavaScript("onedev.server.stepEdit.renderStepIndex();");
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
						stepsView.swap(fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++)
						stepsView.swap(fromIndex-i, fromIndex-i-1);
				}
				target.appendJavaScript("onedev.server.stepEdit.renderStepIndex();");
			}
			
		}.sortable(">.steps").items(".step").handle(".step-head"));
		
	}
	
	protected Component newStepEditor(String componentId, StepEditBean bean) {
		Fragment fragment = new Fragment(componentId, "stepEditFrag", this);
		fragment.add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				stepsView.remove(fragment);
				
				String script = String.format(""
						+ "$('#%s').remove();"
						+ "onedev.server.stepEdit.renderStepIndex();", 
						fragment.getMarkupId());
				target.appendJavaScript(script);
			}
			
		});
		fragment.add(new FencedFeedbackPanel("feedback", fragment));
		fragment.add(PropertyContext.edit("editor", bean, "step"));
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

	@SuppressWarnings("unchecked")
	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (Component each: stepsView) {
			PropertyEditor<Serializable> editor = (PropertyEditor<Serializable>) each.get("editor");
			value.add(editor.getConvertedInput());
		}
		return value;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void error(PathNode propertyNode, Path pathInProperty, String errorMessage) {
		int index = ((Indexed) propertyNode).getIndex();
		PropertyEditor<?> editor = (PropertyEditor<?>) stepsView.get(index).get("editor");
		editor.error(pathInProperty, errorMessage);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new StepResourceReference()));
		response.render(OnDomReadyHeaderItem.forScript("onedev.server.stepEdit.renderStepIndex();"));
	}
	
}

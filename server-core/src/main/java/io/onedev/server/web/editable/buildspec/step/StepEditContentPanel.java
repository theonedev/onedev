package io.onedev.server.web.editable.buildspec.step;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.step.Step;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.link.DropdownLink;
import io.onedev.server.web.component.typeselect.TypeSelectPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import static io.onedev.server.web.component.floating.AlignPlacement.bottom;

abstract class StepEditContentPanel extends Panel {

	private final Step step;
	
	public StepEditContentPanel(String id, Step step) {
		super(id);
		this.step = step;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		form.setOutputMarkupId(true);
		
		var head = new WebMarkupContainer("head");
		head.setOutputMarkupId(true);
		form.add(head);
		
		head.add(new DropdownLink("typeSelector", head, bottom(0), true, true) {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("title", EditableUtils.getGroupedType(step.getClass())));
			}

			@Override
			protected Component newContent(String id, FloatingPanel dropdown) {
				return new TypeSelectPanel<Step>(id) {

					@Override
					protected void onSelect(AjaxRequestTarget target, Class<? extends Step> type) {
						dropdown.close();
						StepEditContentPanel.this.onSelect(target, type);
					}

				};
			}
		});
		
		String description = StringUtils.trimToNull(StringUtils.stripStart(
				EditableUtils.getDescription(step.getClass()), "."));
		if (description != null)
			form.add(new Label("description", description).setEscapeModelStrings(false));
		else
			form.add(new WebMarkupContainer("description").setVisible(false));
		
		form.add(new FencedFeedbackPanel("feedback", form));
		
		BeanEditor editor = BeanContext.edit("editor", step);
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.append("class", "dirty-aware"));
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StepEditContentPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				StepEditContentPanel.this.onCancel(target);
			}
			
		});
		head.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StepEditContentPanel.this));
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				StepEditContentPanel.this.onCancel(target);
			}
			
		});
		add(form);
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSelect(AjaxRequestTarget target, Class<? extends Step> stepType);
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}

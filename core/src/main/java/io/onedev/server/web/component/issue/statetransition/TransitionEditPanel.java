package io.onedev.server.web.component.issue.statetransition;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.util.ajaxlistener.ConfirmLeaveListener;

@SuppressWarnings("serial")
abstract class TransitionEditPanel extends Panel implements InputContext {

	private final List<TransitionSpec> transitions;
	
	private final int transitionIndex;
	
	public TransitionEditPanel(String id, List<TransitionSpec> transitions, int transitionIndex) {
		super(id);
		this.transitions = transitions;
		this.transitionIndex = transitionIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		TransitionSpec transition;
		if (transitionIndex != -1)
			transition = SerializationUtils.clone(transitions.get(transitionIndex));
		else
			transition = new TransitionSpec();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
				RequestCycle.get().find(AjaxRequestTarget.class).add(this);
			}
			
		};
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(TransitionEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.editBean("editor", transition);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (transitionIndex != -1)
					transitions.set(transitionIndex, transition);
				else 
					transitions.add(transition);
				onChanged(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(TransitionEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	public List<String> getInputNames() {
		return getIssueSetting().getFieldNames();
	}
	
	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssueSetting().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	protected abstract void onChanged(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}

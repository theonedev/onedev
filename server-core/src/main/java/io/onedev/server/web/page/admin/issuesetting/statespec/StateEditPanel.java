package io.onedev.server.web.page.admin.issuesetting.statespec;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChanged;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class StateEditPanel extends Panel {

	private final int stateIndex;
	
	public StateEditPanel(String id, int stateIndex) {
		super(id);
	
		this.stateIndex = stateIndex;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		StateSpec state;
		if (stateIndex != -1)
			state = SerializationUtils.clone(getSetting().getStateSpecs().get(stateIndex));
		else
			state = new StateSpec();

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
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StateEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		BeanEditor editor = BeanContext.edit("editor", state);
		form.add(editor);
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				if (stateIndex != -1) { 
					StateSpec oldState = getSetting().getStateSpecs().get(stateIndex);
					if (!state.getName().equals(oldState.getName()) && getSetting().getStateSpec(state.getName()) != null) {
						editor.error(new Path(new PathNode.Named("name")),
								"This name has already been used by another state");
					}
				} else if (getSetting().getStateSpec(state.getName()) != null) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another state");
				}

				if (editor.isValid()) {
					if (stateIndex != -1) {
						StateSpec oldState = getSetting().getStateSpecs().get(stateIndex);
						if (!state.getName().equals(oldState.getName())) { 
							getSetting().setReconciled(false);
							send(getPage(), Broadcast.BREADTH, new WorkflowChanged(target));
						}
						getSetting().getStateSpecs().set(stateIndex, state);
					} else {
						getSetting().getStateSpecs().add(state);
					}
					OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
					onSave(target);
				} else {
					target.add(form);
				}
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(StateEditPanel.this));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.setOutputMarkupId(true);
		
		add(form);
	}

	protected abstract GlobalIssueSetting getSetting();
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);

}

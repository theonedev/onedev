package io.onedev.server.web.page.project.issues.boards;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class MilestoneEditPanel extends Panel {

	private final Long milestoneId;
	
	public MilestoneEditPanel(String id, Long milestoneId) {
		super(id);
		this.milestoneId = milestoneId;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Milestone milestone = getMilestone();
		BeanEditor editor = BeanContext.edit("editor", milestone);
		Form<?> form = new Form<Void>("form");
		form.add(editor);
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				Milestone milestoneWithSameName = milestoneManager.find(milestone.getProject(), milestone.getName());
				if (milestoneWithSameName != null && !milestoneWithSameName.equals(milestone)) {
					editor.error(new Path(new PathNode.Named("name")),
							"This name has already been used by another milestone in the project");
				} 
				if (editor.isValid()){
					Milestone reloaded = getMilestone();
					editor.getDescriptor().copyProperties(milestone, reloaded);
					milestoneManager.save(reloaded);
					Session.get().success("Milestone saved");
					onMilestoneSaved(target, reloaded);
				} else {
					target.add(MilestoneEditPanel.this);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(MilestoneEditPanel.this);
			}
			
		});
		form.add(new AjaxLink<Void>("close") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		add(form);
		
		setOutputMarkupId(true);
	}

	private Milestone getMilestone() {
		return OneDev.getInstance(MilestoneManager.class).load(milestoneId);
	}
	
	protected abstract void onMilestoneSaved(AjaxRequestTarget target, Milestone milestone);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}

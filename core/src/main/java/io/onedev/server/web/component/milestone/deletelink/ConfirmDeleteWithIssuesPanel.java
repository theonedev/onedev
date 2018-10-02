package io.onedev.server.web.component.milestone.deletelink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.issues.milestones.MilestoneDeleteOption;

@SuppressWarnings("serial")
abstract class ConfirmDeleteWithIssuesPanel extends Panel {

	public ConfirmDeleteWithIssuesPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		
		form.add(new Label("title", "Confirm Deleting Milestone '" + getMilestone().getName() + "'"));
		
		MilestoneDeleteOption option = new MilestoneDeleteOption();
		option.setMilestoneToDelete(getMilestone().getName());
		form.add(BeanContext.editBean("editor", option, Sets.newHashSet("milestoneToDelete"), true));
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				MilestoneManager milestoneManager = OneDev.getInstance(MilestoneManager.class);
				if (option.isMoveIssuesToAnotherMilestone()) {
					Milestone newMilestone = milestoneManager.find(getMilestone().getProject(), option.getNewMilestone());
					Preconditions.checkNotNull(newMilestone);
					milestoneManager.delete(getMilestone(), newMilestone);
				} else {
					milestoneManager.delete(getMilestone(), null);
				}
				onMilestoneDeleted(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		add(form);
	}

	protected abstract Milestone getMilestone();
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	protected abstract void onMilestoneDeleted(AjaxRequestTarget target);
}

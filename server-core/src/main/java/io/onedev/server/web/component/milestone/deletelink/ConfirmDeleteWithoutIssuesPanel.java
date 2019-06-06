package io.onedev.server.web.component.milestone.deletelink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.MilestoneManager;
import io.onedev.server.model.Milestone;

@SuppressWarnings("serial")
abstract class ConfirmDeleteWithoutIssuesPanel extends Panel {

	public ConfirmDeleteWithoutIssuesPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("milestone", getMilestone().getName()));
		add(new AjaxLink<Void>("ok") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				OneDev.getInstance(MilestoneManager.class).delete(getMilestone(), null);
				onMilestoneDeleted(target);
			}
			
		});
		add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			
		});
		
	}

	protected abstract Milestone getMilestone();
	
	protected abstract void onClose(AjaxRequestTarget target);
	
	protected abstract void onMilestoneDeleted(AjaxRequestTarget target);

}

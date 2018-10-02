package io.onedev.server.web.component.milestone.closelink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.MilestoneManager;
import io.onedev.server.model.Milestone;

@SuppressWarnings("serial")
abstract class ConfirmCloseWithoutOpenIssuesPanel extends Panel {

	public ConfirmCloseWithoutOpenIssuesPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("milestone", getMilestone().getName()));
		add(new AjaxLink<Void>("ok") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				OneDev.getInstance(MilestoneManager.class).close(getMilestone(), null);
				onClose(target);
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
	
	protected abstract void onMilestoneClosed(AjaxRequestTarget target);

}

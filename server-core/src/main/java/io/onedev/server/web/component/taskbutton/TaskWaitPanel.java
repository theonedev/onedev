package io.onedev.server.web.component.taskbutton;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;

@SuppressWarnings("serial")
abstract class TaskWaitPanel extends Panel {

	private final String message;
	
	public TaskWaitPanel(String id, String message) {
		super(id);
		this.message = message;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("message", message));
		add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		add(new AbstractAjaxTimerBehavior(Duration.ONE_SECOND) {

			@Override
			protected void onTimer(AjaxRequestTarget target) {
				check(target);
			}
			
		});
	}

	protected abstract void check(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
	
}

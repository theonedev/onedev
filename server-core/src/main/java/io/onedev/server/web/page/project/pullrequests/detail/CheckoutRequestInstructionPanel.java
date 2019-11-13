package io.onedev.server.web.page.project.pullrequests.detail;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.PullRequest;

@SuppressWarnings("serial")
abstract class CheckoutRequestInstructionPanel extends Panel {

	public CheckoutRequestInstructionPanel(String id) {
		super(id);
	}

	protected abstract PullRequest getPullRequest();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		add(new Label("targetProjectName", request.getTargetProject().getName()));
		add(new Label("localBranch", "pull" + request.getNumber()));
		
		String fetchCommand = String.format("git fetch origin refs/pull/%d/merge:pull%d", 
				request.getNumber(), request.getNumber());
		add(new Label("fetchCommand", fetchCommand));
		add(new Label("headRef", String.format("refs/pull/%d/head", request.getNumber())));
		
		String checkoutCommand = String.format("git checkout pull%d", request.getNumber());
		add(new Label("checkoutCommand", checkoutCommand));
	}

}

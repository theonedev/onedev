package com.gitplex.server.web.page.project.pullrequest.requestdetail;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.gitplex.server.model.PullRequest;

@SuppressWarnings("serial")
class CheckoutRequestInstructionPanel extends Panel {

	public CheckoutRequestInstructionPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = (PullRequest) getDefaultModelObject();
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

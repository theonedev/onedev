package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Verification;

@SuppressWarnings("serial")
public abstract class VerificationStatusPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final String commitHash;
	
	public VerificationStatusPanel(String id, IModel<PullRequest> requestModel, String commitHash) {
		super(id);
		
		this.requestModel = requestModel;
		this.commitHash = commitHash;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final List<Verification> verifications = new ArrayList<>();
		for (Verification verification: requestModel.getObject().getVerifications()) {
			if (verification.getCommit().equals(commitHash))
				verifications.add(verification);
		}

		Verification.Status overallStatus = null;
		for (Verification verification: verifications) {
			if (verification.getStatus() == Verification.Status.NOT_PASSED) {
				overallStatus = Verification.Status.NOT_PASSED;
				break;
			} else if (verification.getStatus() == Verification.Status.ONGOING) {
				overallStatus = Verification.Status.ONGOING;
			} else if (overallStatus == null) {
				overallStatus = Verification.Status.PASSED;
			}
		}

		DropdownPanel dropdown = new DropdownPanel("details", true) {

			@Override
			protected Component newContent(String id) {
				return new VerificationDetailPanel(id, new AbstractReadOnlyModel<List<Verification>>() {

					@Override
					public List<Verification> getObject() {
						return verifications;
					}
					
				});
			}
			
		};
		add(dropdown);
		
		if (overallStatus != null) {
			Component statusComponent = newStatusComponent("overall", overallStatus);
			statusComponent.add(new DropdownBehavior(dropdown));
			add(statusComponent);
		} else {
			add(new WebMarkupContainer("overall").setVisible(false));
		}
	}
	
	protected abstract Component newStatusComponent(String id, Verification.Status status);
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		super.onDetach();
	}

}

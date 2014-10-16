package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestVerification;
import com.pmease.gitplex.core.model.PullRequestVerification.Status;

@SuppressWarnings("serial")
public abstract class VerificationStatusPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<String> commitHashModel;
	
	public VerificationStatusPanel(String id, IModel<PullRequest> requestModel, IModel<String> commitHashModel) {
		super(id);
		
		this.requestModel = requestModel;
		this.commitHashModel = commitHashModel;
	}
	
	private List<PullRequestVerification> getVerifications() {
		List<PullRequestVerification> verifications = new ArrayList<>();
		for (PullRequestVerification verification: requestModel.getObject().getVerifications()) {
			if (verification.getCommit().equals(commitHashModel.getObject()))
				verifications.add(verification);
		}
		return verifications;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DropdownPanel dropdown = new DropdownPanel("details", true) {

			@Override
			protected Component newContent(String id) {
				return new VerificationDetailPanel(id, new AbstractReadOnlyModel<List<PullRequestVerification>>() {

					@Override
					public List<PullRequestVerification> getObject() {
						return getVerifications();
					}
					
				});
			}
			
		};
		add(dropdown);
		
		Component statusComponent = newStatusComponent("overall", new LoadableDetachableModel<PullRequestVerification.Status>() {

			@Override
			protected Status load() {
				PullRequestVerification.Status overallStatus = null;
				for (PullRequestVerification verification: getVerifications()) {
					if (verification.getStatus() == PullRequestVerification.Status.NOT_PASSED) {
						overallStatus = PullRequestVerification.Status.NOT_PASSED;
						break;
					} else if (verification.getStatus() == PullRequestVerification.Status.ONGOING) {
						overallStatus = PullRequestVerification.Status.ONGOING;
					} else if (overallStatus == null) {
						overallStatus = PullRequestVerification.Status.PASSED;
					}
				}
				return overallStatus;
			}
			
		});
		statusComponent.add(new DropdownBehavior(dropdown));
		add(statusComponent);
	}
	
	protected abstract Component newStatusComponent(String id, IModel<PullRequestVerification.Status> statusModel);
	
	@Override
	protected void onDetach() {
		requestModel.detach();
		commitHashModel.detach();
		super.onDetach();
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!getVerifications().isEmpty());
	}

}

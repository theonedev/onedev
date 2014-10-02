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
import com.pmease.gitplex.core.model.Verification;
import com.pmease.gitplex.core.model.Verification.Status;

@SuppressWarnings("serial")
public abstract class VerificationStatusPanel extends Panel {

	private final IModel<PullRequest> requestModel;
	
	private final IModel<String> commitHashModel;
	
	public VerificationStatusPanel(String id, IModel<PullRequest> requestModel, IModel<String> commitHashModel) {
		super(id);
		
		this.requestModel = requestModel;
		this.commitHashModel = commitHashModel;
	}
	
	private List<Verification> getVerifications() {
		List<Verification> verifications = new ArrayList<>();
		for (Verification verification: requestModel.getObject().getVerifications()) {
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
				return new VerificationDetailPanel(id, new AbstractReadOnlyModel<List<Verification>>() {

					@Override
					public List<Verification> getObject() {
						return getVerifications();
					}
					
				});
			}
			
		};
		add(dropdown);
		
		Component statusComponent = newStatusComponent("overall", new LoadableDetachableModel<Verification.Status>() {

			@Override
			protected Status load() {
				Verification.Status overallStatus = null;
				for (Verification verification: getVerifications()) {
					if (verification.getStatus() == Verification.Status.NOT_PASSED) {
						overallStatus = Verification.Status.NOT_PASSED;
						break;
					} else if (verification.getStatus() == Verification.Status.ONGOING) {
						overallStatus = Verification.Status.ONGOING;
					} else if (overallStatus == null) {
						overallStatus = Verification.Status.PASSED;
					}
				}
				return overallStatus;
			}
			
		});
		statusComponent.add(new DropdownBehavior(dropdown));
		add(statusComponent);
	}
	
	protected abstract Component newStatusComponent(String id, IModel<Verification.Status> statusModel);
	
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

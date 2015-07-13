package com.pmease.gitplex.web.component.pullrequest.verificationstatus;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;

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
		
		DropdownPanel dropdown = new DropdownPanel("detail", true) {

			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "detailFrag", VerificationStatusPanel.this);
				
				IModel<List<PullRequestVerification>> model = new AbstractReadOnlyModel<List<PullRequestVerification>>() {

					@Override
					public List<PullRequestVerification> getObject() {
						return getVerifications();
					}
					
				};
				fragment.add(new ListView<PullRequestVerification>("verifications", model) {

					@Override
					protected void populateItem(ListItem<PullRequestVerification> item) {
						PullRequestVerification verification = item.getModelObject();
						item.add(new Label("configuration", verification.getConfiguration()));
						item.add(new Label("message", verification.getMessage()).setEscapeModelStrings(false));
						
						if (verification.getStatus() == PullRequestVerification.Status.NOT_PASSED)
							item.add(AttributeAppender.append("class", "not-passed"));
						else if (verification.getStatus() == PullRequestVerification.Status.PASSED)
							item.add(AttributeAppender.append("class", "passed"));
						else
							item.add(AttributeAppender.append("class", "ongoing"));
					}
					
				});
				
				return fragment;
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(VerificationStatusPanel.class, "verification-status.css")));
	}

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

package com.gitplex.server.web.component.pullrequest.verificationstatus;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.commons.wicket.component.DropdownLink;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestVerification;

@SuppressWarnings("serial")
public class VerificationStatusPanel extends Panel {

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
		
		DropdownLink link = new DropdownLink("link") {

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
						
						if (verification.getStatus() == PullRequestVerification.Status.FAILED)
							item.add(AttributeAppender.append("class", "failed"));
						else if (verification.getStatus() == PullRequestVerification.Status.SUCCESSFUL)
							item.add(AttributeAppender.append("class", "successful"));
						else
							item.add(AttributeAppender.append("class", "running"));
					}
					
				});
				
				return fragment;
			}
			
		};
		add(link);

		link.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequestVerification.Status overallStatus = getOverallStatus();				
				if (overallStatus == PullRequestVerification.Status.SUCCESSFUL) {
					return "successful";
				} else if (overallStatus == PullRequestVerification.Status.FAILED) {
					return "failed";
				} else {
					return "running";
				}
			}
			
		}));
		link.add(AttributeAppender.append("title", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequestVerification.Status overallStatus = getOverallStatus();				
				if (overallStatus == PullRequestVerification.Status.SUCCESSFUL) {
					return "Builds are successful";
				} else if (overallStatus == PullRequestVerification.Status.FAILED) {
					return "At least one build is failed";
				} else {
					return "Builds are running";
				}
			}
			
		}));
		
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		link.add(icon);
		icon.add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				PullRequestVerification.Status overallStatus = getOverallStatus();				
				if (overallStatus == PullRequestVerification.Status.SUCCESSFUL) {
					return "fa fa-check";
				} else if (overallStatus == PullRequestVerification.Status.FAILED) {
					return "fa fa-times";
				} else {
					return "fa fa-clock-o";
				}
			}
			
		}));
		
		setOutputMarkupPlaceholderTag(true);
	}
	
	private PullRequestVerification.Status getOverallStatus() {
		PullRequestVerification.Status overallStatus = null;
		for (PullRequestVerification verification: getVerifications()) {
			if (verification.getStatus() == PullRequestVerification.Status.FAILED) {
				overallStatus = PullRequestVerification.Status.FAILED;
				break;
			} else if (verification.getStatus() == PullRequestVerification.Status.RUNNING) {
				overallStatus = PullRequestVerification.Status.RUNNING;
			} else if (overallStatus == null) {
				overallStatus = PullRequestVerification.Status.SUCCESSFUL;
			}
		}
		return overallStatus;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(new VerificationStatusResourceReference()));
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

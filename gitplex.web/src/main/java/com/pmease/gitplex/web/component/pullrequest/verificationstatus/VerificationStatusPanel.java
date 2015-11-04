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

import com.pmease.commons.wicket.component.DropdownLink;
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
		
		DropdownLink link =  new DropdownLink("link") {

			@Override
			protected Component newContent(String id) {
				Fragment fragment = new Fragment(id, "detailFrag", VerificationStatusPanel.this);
				
				IModel<List<Verification>> model = new AbstractReadOnlyModel<List<Verification>>() {

					@Override
					public List<Verification> getObject() {
						return getVerifications();
					}
					
				};
				fragment.add(new ListView<Verification>("verifications", model) {

					@Override
					protected void populateItem(ListItem<Verification> item) {
						Verification verification = item.getModelObject();
						item.add(new Label("configuration", verification.getConfiguration()));
						item.add(new Label("message", verification.getMessage()).setEscapeModelStrings(false));
						
						if (verification.getStatus() == Verification.Status.NOT_PASSED)
							item.add(AttributeAppender.append("class", "not-passed"));
						else if (verification.getStatus() == Verification.Status.PASSED)
							item.add(AttributeAppender.append("class", "passed"));
						else
							item.add(AttributeAppender.append("class", "ongoing"));
					}
					
				});
				
				return fragment;
			}
			
		};
		add(link);
		
		link.add(newStatusComponent("overall", new LoadableDetachableModel<Verification.Status>() {

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
			
		}));
	}
	
	protected abstract Component newStatusComponent(String id, IModel<Verification.Status> statusModel);
	
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

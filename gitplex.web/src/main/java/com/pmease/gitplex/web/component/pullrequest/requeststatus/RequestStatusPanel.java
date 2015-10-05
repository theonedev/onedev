package com.pmease.gitplex.web.component.pullrequest.requeststatus;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.behavior.TooltipBehavior;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequest.CloseStatus;
import com.pmease.gitplex.core.model.PullRequest.Status;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;

@SuppressWarnings("serial")
public class RequestStatusPanel extends Panel {

	private final boolean checkGatekeeper;
	
	public RequestStatusPanel(String id, IModel<PullRequest> requestModel, boolean checkGatekeeper) {
		super(id, requestModel);
		this.checkGatekeeper = checkGatekeeper;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("status", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getPullRequest().getCloseStatus() == CloseStatus.INTEGRATED)
					return "Integrated";
				else if (getPullRequest().getCloseStatus() == CloseStatus.DISCARDED)
					return "Discarded";
				else if (checkGatekeeper) {
					
				} else 
					return "
			}
			
		}).add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Status status = getPullRequest().getStatus();
				if (status == Status.DISCARDED)
					return "label-danger";
				else if (status == Status.INTEGRATED)
					return "label-success";
				else 
					return "label-warning";
			}
			
		})));
		
		add(new WebMarkupContainer("help").add(new TooltipBehavior(new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				Status status = getPullRequest().getStatus();
				if (status == Status.PENDING_INTEGRATE) {
					return "The pull request is waiting to be integrated (merge or rebase based "
							+ "on integration strategy) into target branch. Anyone with "
							+ "repository write permission will be able to do this by clicking "
							+ "the integrate button";
				} else if (status == Status.PENDING_APPROVAL) {
					return "As result of gate keeper setting of the repository, the pull request "
							+ "is waiting for approval of someone before it can be integrated "
							+ "into target branch";
				} else if (status == Status.PENDING_UPDATE) {
					return "The pull request is currently rejected by gate keeper of the "
							+ "repository, and is expecting the submitter to update the pull "
							+ "request with new commits";
				} else if (status == Status.INTEGRATED) {
					return "The pull request has been successfully integrated into target branch "
							+ "(merged or rebased based on integration strategy)";
				} else {
					return "The pull request is discarded and commits from its source branch will "
							+ "not be integrated into target branch";
				}
			}
			
		}, new TooltipConfig().withPlacement(TooltipConfig.Placement.left))));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}

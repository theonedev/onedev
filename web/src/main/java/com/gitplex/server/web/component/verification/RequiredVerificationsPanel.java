package com.gitplex.server.web.component.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.gitplex.server.model.PullRequest;
import com.gitplex.server.util.Verification;
import com.gitplex.server.util.Verification.Status;
import com.gitplex.server.web.websocket.PageDataChanged;

@SuppressWarnings("serial")
public class RequiredVerificationsPanel extends GenericPanel<PullRequest> {

	private final IModel<List<String>> verificationsModel;
	
	public RequiredVerificationsPanel(String id, IModel<PullRequest> model) {
		super(id, model);

		verificationsModel = new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				PullRequest request = getPullRequest();
				Map<String, Verification> effectiveVerifications = 
						request.getQualityCheckStatus().getEffectiveVerifications();
				List<String> requiredVerifications = new ArrayList<>(effectiveVerifications.keySet());
				Collections.sort(requiredVerifications, new Comparator<String>() {

					@Override
					public int compare(String o1, String o2) {
						return effectiveVerifications.get(o1).getDate()
								.compareTo(effectiveVerifications.get(o2).getDate());
					}
					
				});
				requiredVerifications.addAll(request.getQualityCheckStatus().getAwaitingVerifications());
				return requiredVerifications;
			}
			
		};
	}

	private PullRequest getPullRequest() {
		return getModelObject();
	}
	
	@Override
	protected void onDetach() {
		verificationsModel.detach();
		super.onDetach();
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);

		if (event.getPayload() instanceof PageDataChanged && isVisibleInHierarchy()) {
			PageDataChanged pageDataChanged = (PageDataChanged) event.getPayload();
			pageDataChanged.getHandler().add(this);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ListView<String>("verifications", verificationsModel) {

			@Override
			protected void populateItem(ListItem<String> item) {
				PullRequest request = getPullRequest();
				String verificationName = item.getModelObject();
				item.add(new Label("name", verificationName));
				
				Verification verification = 
						request.getQualityCheckStatus().getEffectiveVerifications().get(verificationName);

				WebMarkupContainer result = new WebMarkupContainer("result");
				if (verification != null) {
					if (verification.getDescription() != null) {
						if (verification.getTargetUrl() != null) {
							Fragment fragment = new Fragment("content", "linkFrag", RequiredVerificationsPanel.this);
							ExternalLink link = new ExternalLink("link", verification.getTargetUrl());
							link.add(new Label("label", verification.getDescription()));
							fragment.add(link);
							item.add(fragment);
						} else {
							item.add(new Label("content", verification.getDescription()));
						}
					} else {
						item.add(new Label("content").setVisible(false));
					}
					if (verification.getStatus() == Status.SUCCESS) {
						result.add(AttributeAppender.append("class", "success fa fa-check"));
						result.add(AttributeAppender.append("title", "Verification is successful"));
					} else if (verification.getStatus() == Status.ERROR) {
						result.add(AttributeAppender.append("class", "error fa fa-warning"));
						result.add(AttributeAppender.append("title", "Verification is in error"));
					} else if (verification.getStatus() == Status.FAILURE) {
						result.add(AttributeAppender.append("class", "failure fa fa-times"));
						result.add(AttributeAppender.append("title", "Verification is failed"));
					} else {
						result.add(AttributeAppender.append("class", "running fa fa-circle"));
						result.add(AttributeAppender.append("title", "Verification is running"));
					}
				} else {
					item.add(new Label("content").setVisible(false));
					result.add(AttributeAppender.append("class", "awaiting fa fa-clock-o"));
					result.add(AttributeAppender.append("title", "Waiting for verification"));
				}
				
				item.add(result);
			}
			
		});
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(!verificationsModel.getObject().isEmpty());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(new VerificationResourceReference()));
	}

}

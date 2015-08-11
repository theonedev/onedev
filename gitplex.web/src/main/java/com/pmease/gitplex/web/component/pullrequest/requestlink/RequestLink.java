package com.pmease.gitplex.web.component.pullrequest.requestlink;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview.RequestOverviewPage;

@SuppressWarnings("serial")
public class RequestLink extends Panel {

	public RequestLink(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("link", RequestOverviewPage.class) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				add(new Label("label", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return getPullRequest().getTitle();
					}
					
				}));
			}

			@Override
			public PageParameters getPageParameters() {
				return RequestOverviewPage.paramsOf(getPullRequest());
			}
			
		});
	}
	
	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}

}

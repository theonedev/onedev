package com.pmease.gitplex.web.page.repository.pullrequest;

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.web.component.branch.BranchLink;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;

@SuppressWarnings("serial")
public class RequestSummaryPanel extends Panel {

	public RequestSummaryPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		BookmarkablePageLink<Void> titleLink = new BookmarkablePageLink<Void>("titleLink", RequestOverviewPage.class) {

				@Override
				public PageParameters getPageParameters() {
					return RequestOverviewPage.paramsOf(getPullRequest());
				}
			
		};

		add(titleLink);
		
		titleLink.add(new Label("titleLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getTitle();
			}
			
		}));

		add(new MultiLineLabel("description", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getDescription();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getDescription() != null);
			}
			
		});
		
		add(new UserLink("user", Model.of(getPullRequest().getSubmitter()), AvatarMode.NAME_AND_AVATAR));
		
		add(new BranchLink("target", new AbstractReadOnlyModel<Branch>() {

			@Override
			public Branch getObject() {
				return getPullRequest().getTarget();
			}
			
		}));

		add(new BranchLink("source", new AbstractReadOnlyModel<Branch>() {

			@Override
			public Branch getObject() {
				return getPullRequest().getSource();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getSource() != null);
			}
			
		});
		
		add(new AgeLabel("date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getPullRequest().getCreateDate();
			}
			
		}));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}

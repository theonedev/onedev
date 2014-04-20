package com.pmease.gitop.web.page.repository.pullrequest;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.PersonIdent;

import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.PersonLink;
import com.pmease.gitop.web.component.link.PersonLink.Mode;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;

@SuppressWarnings("serial")
public class RequestSummaryPanel extends Panel {

	public RequestSummaryPanel(String id, IModel<PullRequest> requestModel) {
		super(id, requestModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Link<Void> titleLink = new Link<Void>("titleLink") {

			@Override
			public void onClick() {
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				PageParameters params = PageSpec.forRepository(page.getRepository());
				params.set(0, getPullRequest().getId());
				setResponsePage(RequestDetailPage.class, params);
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
		
		add(new PersonLink("user", new LoadableDetachableModel<PersonIdent>() {

			@Override
			protected PersonIdent load() {
				return getPullRequest().getSubmittedBy().asPerson();
			}
			
		}, Mode.NAME_AND_AVATAR));
		
		Link<Void> branchLink = new Link<Void>("branchLink") {

			@Override
			public void onClick() {
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				Branch source = getPullRequest().getSource();
				setVisible(source != null);
				setEnabled(SecurityUtils.getSubject().isPermitted(
						ObjectPermission.ofRepositoryRead(source.getRepository())));
			}
			
		};
		add(branchLink);
		branchLink.add(new Label("branchLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				if (page.getRepository().equals(request.getSource().getRepository())) {
					return request.getSource().getName();
				} else {
					return request.getSource().getRepository().toString() + ":" + request.getSource().getName();
				}
			}
			
		}));
		
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

package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.util.DateUtils;

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
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				PageParameters params = PageSpec.forProject(page.getProject());
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
		
		add(new GitPersonLink("user", new LoadableDetachableModel<GitPerson>() {

			@Override
			protected GitPerson load() {
				User user = getPullRequest().getSubmitter();
				return new GitPerson(user.getName(), user.getEmail());
			}
			
		}, Mode.FULL));
		
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
						ObjectPermission.ofProjectRead(source.getProject())));
			}
			
		};
		add(branchLink);
		branchLink.add(new Label("branchLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				if (page.getProject().equals(request.getSource().getProject())) {
					return request.getSource().getName();
				} else {
					return request.getSource().getProject().toString() + ":" + request.getSource().getName();
				}
			}
			
		}));
		
		add(new Label("date", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getPullRequest().getCreateDate());
			}
			
		}));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
}

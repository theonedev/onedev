package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.api.GitPerson;

@SuppressWarnings("serial")
public class OpenActivityPanel extends Panel {

	public OpenActivityPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GitPersonLink("user", new LoadableDetachableModel<GitPerson>() {

			@Override
			protected GitPerson load() {
				User user = getPullRequest().getSubmittedBy();
				return new GitPerson(user.getName(), user.getEmail());
			}
			
		}, Mode.FULL));
		
		Link<Void> targetLink = new Link<Void>("targetLink") {

			@Override
			public void onClick() {
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				Branch target = getPullRequest().getTarget();
				setEnabled(SecurityUtils.getSubject().isPermitted(
						ObjectPermission.ofProjectRead(target.getProject())));
			}
			
		};
		add(targetLink);
		targetLink.add(new Label("targetLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				Branch target = request.getTarget();
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				if (page.getProject().equals(target.getProject())) {
					return target.getName();
				} else {
					return target.getProject().toString() + ":" + target.getName();
				}
			}
			
		}));
		
		Link<Void> sourceLink = new Link<Void>("sourceLink") {

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
		add(sourceLink);
		sourceLink.add(new Label("sourceLabel", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				Branch source = request.getSource();
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				if (page.getProject().equals(source.getProject())) {
					return source.getName();
				} else {
					return source.getProject().toString() + ":" + source.getName();
				}
			}
			
		}));
		
		add(new AgeLabel("date", new AbstractReadOnlyModel<Date>() {

			@Override
			public Date getObject() {
				return getPullRequest().getCreateDate();
			}
			
		}));

		add(new WebMarkupContainer("body"));
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
	
}

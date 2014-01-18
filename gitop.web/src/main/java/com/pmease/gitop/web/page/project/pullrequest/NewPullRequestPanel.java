package com.pmease.gitop.web.page.project.pullrequest;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.web.component.comparablebranchselector.ComparableBranchSelector;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public class NewPullRequestPanel extends Panel {

	private final PullRequest pullRequest;
	
	public NewPullRequestPanel(String id, PullRequest pullRequest) {
		super(id);

		this.pullRequest = pullRequest;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		IModel<Project> currentProjectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				return page.getProject();
			}
			
		};
		Form<Void> form = new Form<Void>("form");
		form.add(new TextField<String>("title", new PropertyModel<String>(pullRequest, "title")).setRequired(true));
		form.add(new ComparableBranchSelector("target", currentProjectModel, 
				new PropertyModel<Branch>(pullRequest, "target")).setRequired(true));
		
		form.add(new ComparableBranchSelector("source", currentProjectModel, 
				new PropertyModel<Branch>(pullRequest, "source")).setRequired(true));
		
		form.add(new SubmitLink("submit") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				Gitop.getInstance(PullRequestManager.class).save(pullRequest);
			}
			
		});
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				setResponsePage(PullRequestsPage.class, PageSpec.forProject(page.getProject()));
			}
			
		});
		add(form);
	}
	
}

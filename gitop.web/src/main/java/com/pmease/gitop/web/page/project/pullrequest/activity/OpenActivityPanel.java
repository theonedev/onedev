package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.pmease.commons.git.Commit;
import com.pmease.commons.git.Git;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.AuthorizationManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.page.project.api.GitPerson;
import com.pmease.gitop.web.page.project.pullrequest.CommitListPanel;
import com.pmease.gitop.web.util.DateUtils;

@SuppressWarnings("serial")
public class OpenActivityPanel extends Panel {

	private String description;
	
	public OpenActivityPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}
	
	private Fragment renderForView() {
		Fragment fragment = new Fragment("body", "viewFrag", this);

		description = getPullRequest().getDescription();
		if (StringUtils.isNotBlank(description))
			fragment.add(new MultiLineLabel("description", description));
		else
			fragment.add(new Label("description", "<i>No description</i>").setEscapeModelStrings(false));
		
		fragment.add(new CommitListPanel("commits", new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = getPullRequest();
				Git git = request.getTarget().getProject().git();
				
				return git.log(request.getTarget().getHeadCommit(), 
						request.getInitialUpdate().getHeadCommit(), 
						null, 0, 0); 
			}
			
		}));
		
		return fragment;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		PullRequest request = getPullRequest();
		User submittedBy = request.getSubmittedBy();
		if (submittedBy != null) {
			GitPerson person = new GitPerson(submittedBy.getName(), submittedBy.getEmail());
			add(new GitPersonLink("user", Model.of(person), GitPersonLink.Mode.NAME_AND_AVATAR));
		} else {
			add(new Label("<i>Unknown</i>").setEscapeModelStrings(false));
		}
		
		add(new Label("date", DateUtils.formatAge(request.getCreateDate())));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				description = getPullRequest().getDescription();
				
				Fragment fragment = new Fragment("body", "editFrag", OpenActivityPanel.this);
				
				final TextArea<String> descriptionArea = new TextArea<String>("description", new IModel<String>() {

					@Override
					public void detach() {
					}

					@Override
					public String getObject() {
						return description;
					}

					@Override
					public void setObject(String object) {
						description = object;
					}

				});
				
				descriptionArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

					@Override
					protected void onUpdate(AjaxRequestTarget target) {
						descriptionArea.processInput();
					}
					
				});
				
				fragment.add(descriptionArea);
				
				fragment.add(new AjaxLink<Void>("save") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						PullRequest request = getPullRequest();
						request.setDescription(description);
						Gitop.getInstance(PullRequestManager.class).save(request);

						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(OpenActivityPanel.this);
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("cancel") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Fragment fragment = renderForView();
						OpenActivityPanel.this.replace(fragment);
						target.add(OpenActivityPanel.this);
					}
					
				});
				
				OpenActivityPanel.this.replace(fragment);
				
				target.add(OpenActivityPanel.this);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(Gitop.getInstance(AuthorizationManager.class)
						.canModify(getPullRequest()));
			}

		});
		
		add(renderForView());

		setOutputMarkupId(true);
	}

	private PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}
	
}

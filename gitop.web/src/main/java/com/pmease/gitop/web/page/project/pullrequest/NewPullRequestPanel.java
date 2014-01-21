package com.pmease.gitop.web.page.project.pullrequest;

import static com.pmease.gitop.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_APPROVAL;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_UPDATE;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.comparablebranchselector.ComparableBranchSelector;
import com.pmease.gitop.web.model.EntityModel;
import com.pmease.gitop.web.page.project.AbstractProjectPage;

@SuppressWarnings("serial")
public class NewPullRequestPanel extends Panel {

	@SuppressWarnings("unused")
	private String title;
	
	@SuppressWarnings("unused")
	private String comment;
	
	private IModel<Branch> targetModel;
	
	private IModel<Branch> sourceModel;
	
	private IModel<User> submitterModel;
	
	private IModel<PullRequest> pullRequestModel;
	
	public NewPullRequestPanel(String id, Branch target, Branch source, User submitter) {
		super(id);

		targetModel = new EntityModel<Branch>(target);
		sourceModel = new EntityModel<Branch>(source);
		submitterModel = new EntityModel<User>(submitter);
		
		pullRequestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				PullRequest request = Gitop.getInstance(PullRequestManager.class).findOpen(getTarget(), getSource());
				if (request != null) {
					return request;
				} else {
					request = new PullRequest();
					request.setTarget(getTarget());
					request.setSource(getSource());
					request.setSubmitter(getSubmitter());
					Gitop.getInstance(PullRequestManager.class).refresh(request);
					
					return request;
				}
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		IModel<Project> currentProjectModel = new LoadableDetachableModel<Project>() {

			@Override
			protected Project load() {
				AbstractProjectPage page = (AbstractProjectPage) getPage();
				return page.getProject();
			}
			
		};
		Form<Void> form = new Form<Void>("form");

		final WebMarkupContainer statusContainer = new WebMarkupContainer("status");
		form.add(statusContainer);
		
		form.add(new ComparableBranchSelector("target", currentProjectModel, targetModel, "Target Project", "Target Branch") {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				target.add(statusContainer);
			}
			
		}.setRequired(true));
		
		form.add(new ComparableBranchSelector("source", currentProjectModel, sourceModel, "Source Project", "Source Branch") {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				target.add(statusContainer);
			}
			
		}.setRequired(true));

		WebMarkupContainer messageContainer = new WebMarkupContainer("message") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isNew() && (request.getStatus() == PENDING_APPROVAL || request.getStatus() == PENDING_INTEGRATE)); 
			}
			
		};
		form.add(messageContainer);
		
		messageContainer.add(new TextField<String>("title", new PropertyModel<String>(this, "title")).setRequired(true));
		messageContainer.add(new TextArea<String>("comment", new PropertyModel<String>(this, "comment")));

		statusContainer.add(new SubmitLink("send") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isNew());
				setEnabled(request.getStatus() == PENDING_APPROVAL || request.getStatus() == PENDING_INTEGRATE);
			}

			@Override
			public void onSubmit() {
				super.onSubmit();
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				PullRequest request = getPullRequest();
				if (request.getStatus() != PENDING_APPROVAL && request.getStatus() != PENDING_INTEGRATE)
					tag.put("disabled", "disabled");
			}
			
		});
		
		statusContainer.add(new Link<Void>("view") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isNew());
			}

			@Override
			public void onClick() {
			}
			
		});
		
		statusContainer.add(new Label("summary", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getTarget().equals(getSource())) {
					return "Please select different branches to pull.";
				} else {
					PullRequest request = getPullRequest();
					if (request.isNew()) {
						if (request.getStatus() == INTEGRATED) {
							return "No changes to pull.";
						} else if (request.getStatus() == PENDING_UPDATE) {
							return "Gate keeper of target project rejects the pull request.";
						} else if (request.getMergeResult().getMergeHead() == null) {
							return "There are merge conflicts.";
						} else {
							return "Be able to merge automatically.";
						}
					} else {
						return "A pull request is already open for selected branches.";
					}
				}
			}
			
		}));
		
		statusContainer.add(new Label("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.isNew()) {
					return "#" + request.getId() + ": " + request.getTitle();
				} else if (!getTarget().equals(getSource())) {
					if (request.getStatus() == INTEGRATED) {
						return "Target branch '" + getTarget().getName() + "' of project '" + getTarget().getProject() 
								+ "' is already update to date.";
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
			
		}));
		
		statusContainer.add(new ListView<String>("rejectReasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return getPullRequest().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = getPullRequest();
				setVisible(request.isNew() && request.getStatus() == PENDING_UPDATE);
			}
			
		});

		add(form);
	}
	
	private Branch getTarget() {
		return targetModel.getObject();
	}
	
	private Branch getSource() {
		return sourceModel.getObject();
	}
	
	private User getSubmitter() {
		return submitterModel.getObject();
	}
	
	private PullRequest getPullRequest() {
		return pullRequestModel.getObject();
	}

	@Override
	protected void onDetach() {
		pullRequestModel.detach();
		targetModel.detach();
		sourceModel.detach();
		submitterModel.detach();

		super.onDetach();
	}
	
}

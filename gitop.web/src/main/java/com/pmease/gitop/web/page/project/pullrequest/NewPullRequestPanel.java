package com.pmease.gitop.web.page.project.pullrequest;

import static com.pmease.gitop.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_APPROVAL;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_UPDATE;

import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.commit.CommitsTablePanel;
import com.pmease.gitop.web.component.comparablebranchselector.ComparableBranchSelector;
import com.pmease.gitop.web.model.EntityModel;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.source.commit.diff.DiffViewPanel;

@SuppressWarnings("serial")
public class NewPullRequestPanel extends Panel {

	private static final String IMPOSSIBLE_TITLE = "*[@TITLE IMPOSSIBLE@]*";
	
	private String title = IMPOSSIBLE_TITLE;
	
	@SuppressWarnings("unused")
	private String comment;
	
	private IModel<Branch> targetModel, sourceModel;
	
	private IModel<User> submitterModel;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> pullRequestModel;
	
	private MarkupContainer feedbackContainer, commitsPanel, diffViewPanel;
	
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
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = getPullRequest();
				return getSource().getProject().code().log(request.getMergeResult().getMergeBase(), 
						getSource().getHeadCommit(), null, 0, 0);
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
		
		add(new ComparableBranchSelector("target", currentProjectModel, targetModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				onBranchChange(target);
			}
			
		}.setRequired(true));
		
		add(new ComparableBranchSelector("source", currentProjectModel, sourceModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				onBranchChange(target);
			}
			
		}.setRequired(true));

		add(feedbackContainer = new WebMarkupContainer("feedback"));
		feedbackContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.isNew()) {
					if (getTarget().equals(getSource())) {
						return "warning";
					} else if (request.getStatus() == INTEGRATED) {
						return "info";
					} else if (request.getStatus() == PENDING_UPDATE) {
						return "warning";
					} else if (request.getMergeResult().getMergeHead() == null) {
						return "warning";
					} else {
						return "success";
					}
				} else {
					return "info";
				}
			}
			
		}));
		feedbackContainer.setOutputMarkupId(true);
		
		WebMarkupContainer messageContainer;
		feedbackContainer.add(messageContainer = new WebMarkupContainer("message") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isNew() && (request.getStatus() == PENDING_APPROVAL || request.getStatus() == PENDING_INTEGRATE)); 
			}
			
		});

		final TextField<String> titleField = new TextField<String>("title", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return getTitle();
			}

			@Override
			public void setObject(String object) {
				if (!getDefaultTitle().equals(object))
					title = object;
			}
			
		});
		titleField.add(new AjaxFormComponentUpdatingBehavior("blur") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				titleField.processInput();
			}
			
		});
		messageContainer.add(titleField);
		
		final TextArea<String> commentArea = new TextArea<String>("comment", new PropertyModel<String>(this, "comment"));
		commentArea.add(new AjaxFormComponentUpdatingBehavior("blur") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				commentArea.processInput();
			}
			
		});
		messageContainer.add(commentArea);

		feedbackContainer.add(new Link<Void>("send") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isNew() 
						&& (request.getStatus() == PENDING_APPROVAL || request.getStatus() == PENDING_INTEGRATE));
			}

			@Override
			public void onClick() {
				String title = getTitle();
				if (title != null) {
					
				}
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				PullRequest request = getPullRequest();
				if (request.getStatus() != PENDING_APPROVAL && request.getStatus() != PENDING_INTEGRATE)
					tag.put("disabled", "disabled");
			}
			
		});
		
		feedbackContainer.add(new Link<Void>("view") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getPullRequest().isNew());
			}

			@Override
			public void onClick() {
			}
			
		});
		
		feedbackContainer.add(new Label("summary", new AbstractReadOnlyModel<String>() {

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
		
		feedbackContainer.add(new Label("detail", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (!request.isNew()) {
					return "#" + request.getId() + ": " + request.getTitle();
				} else if (!getTarget().equals(getSource())) {
					if (request.getStatus() == INTEGRATED) {
						return "Target branch '" + getTarget().getName() + "' of project '" + getTarget().getProject() 
								+ "' is already update to date.";
					} else if (title != null 
							&& request.getStatus() != PENDING_UPDATE 
							&& request.getMergeResult().getMergeHead() == null) {
						return "But you can still send the pull request.";
					}
				} 
				return null;
			}
			
		}));
		
		feedbackContainer.add(new ListView<String>("rejectReasons", new LoadableDetachableModel<List<String>>() {

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
		
		IModel<Project> projectModel = new AbstractReadOnlyModel<Project>() {

			@Override
			public Project getObject() {
				return getTarget().getProject();
			}
			
		};
		add(commitsPanel = new CommitsTablePanel("commits", commitsModel, projectModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getStatus() != INTEGRATED);
			}
			
		});
		commitsPanel.setOutputMarkupPlaceholderTag(true);
		
		add(diffViewPanel = new DiffViewPanel("changes", projectModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getMergeResult().getMergeBase();
			}
			
		}, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getSource().getHeadCommit();
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(getPullRequest().getStatus() != INTEGRATED);
			}
			
		});
		diffViewPanel.setOutputMarkupPlaceholderTag(true);
	}
	
	private void onBranchChange(AjaxRequestTarget target) {
		target.add(feedbackContainer);
		target.add(commitsPanel);
		target.add(diffViewPanel);
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
	
	private List<Commit> getCommits() {
		return commitsModel.getObject();
	}
	
	private String getDefaultTitle() {
		Preconditions.checkState(!getCommits().isEmpty());
		if (getCommits().size() == 1)
			return getCommits().get(0).getSubject();
		else
			return getSource().getName();
	}

	@Override
	protected void onDetach() {
		pullRequestModel.detach();
		targetModel.detach();
		sourceModel.detach();
		submitterModel.detach();
		commitsModel.detach();

		super.onDetach();
	}

	private String getTitle() {
		if (IMPOSSIBLE_TITLE.equals(title))
			return getDefaultTitle();
		else
			return title;
	}
}

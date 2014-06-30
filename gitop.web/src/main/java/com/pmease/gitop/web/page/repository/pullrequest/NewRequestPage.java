package com.pmease.gitop.web.page.repository.pullrequest;

import static com.pmease.gitop.model.PullRequest.Status.INTEGRATED;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_APPROVAL;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_INTEGRATE;
import static com.pmease.gitop.model.PullRequest.Status.PENDING_UPDATE;

import java.io.File;
import java.util.ArrayList;
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
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Commit;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.ExceptionUtils;
import com.pmease.commons.util.FileUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.component.commit.CommitsTablePanel;
import com.pmease.gitop.web.component.comparablebranchselector.ComparableBranchSelector;
import com.pmease.gitop.web.model.EntityModel;
import com.pmease.gitop.web.page.PageSpec;
import com.pmease.gitop.web.page.repository.RepositoryBasePage;
import com.pmease.gitop.web.page.repository.RepositoryPage;
import com.pmease.gitop.web.page.repository.source.commit.diff.CommitCommentsAware;
import com.pmease.gitop.web.page.repository.source.commit.diff.DiffViewPanel;

@SuppressWarnings("serial")
public class NewRequestPage extends RepositoryPage implements CommitCommentsAware {

	private static final String IMPOSSIBLE_TITLE = "*[@TITLE IMPOSSIBLE@]*";
	
	private String title = IMPOSSIBLE_TITLE;
	
	private String comment;
	
	private IModel<Branch> targetModel, sourceModel;
	
	private IModel<User> submitterModel;
	
	private IModel<List<Commit>> commitsModel;
	
	private IModel<PullRequest> pullRequestModel;
	
	private MarkupContainer feedbackContainer, commitsPanel, diffViewPanel;
	
	public static PageParameters newParams(Repository repository, String source, String dest) {
		PageParameters params = PageSpec.forRepository(repository);
		params.set("source", source);
		params.set("dest", dest);
		return params;
	}
	
	public NewRequestPage(PageParameters params) {
		super(params);
		
		Branch target, source = null;
		BranchManager branchManager = Gitop.getInstance(BranchManager.class);
		RepositoryBasePage page = (RepositoryBasePage) getPage();
		if (page.getRepository().getForkedFrom() != null) {
			target = branchManager.findDefault(page.getRepository().getForkedFrom());
			source = branchManager.findDefault(page.getRepository());
		} else {
			target = branchManager.findDefault(page.getRepository());
			for (Branch each: page.getRepository().getBranches()) {
				if (!each.equals(target)) {
					source = each;
					break;
				}
			}
			if (source == null)
				source = target;
		}
		User currentUser = AppLoader.getInstance(UserManager.class).getCurrent();
		
		targetModel = new EntityModel<Branch>(target);
		sourceModel = new EntityModel<Branch>(source);
		submitterModel = new EntityModel<User>(currentUser);
		
		pullRequestModel = new LoadableDetachableModel<PullRequest>() {

			@Override
			protected PullRequest load() {
				PullRequest request = Gitop.getInstance(PullRequestManager.class).findOpen(getTarget(), getSource());
				if (request != null) {
					return request;
				} else {
					File sandbox = FileUtils.createTempDir();
					try {
						return Gitop.getInstance(PullRequestManager.class).preview(getTarget(), getSource(), 
								getSubmitter(), sandbox);
					} catch (Exception e) {
						FileUtils.deleteDir(sandbox);
						throw ExceptionUtils.unchecked(e);
					}
				}
			}

			@Override
			protected void onDetach() {
				PullRequest request = getObject();
				if (request.getSandbox() != null) {
					FileUtils.deleteDir(request.getSandbox().repoDir());
					request.setSandbox(null);
				}
				super.onDetach();
			}
			
		};
		
		commitsModel = new LoadableDetachableModel<List<Commit>>() {

			@Override
			protected List<Commit> load() {
				PullRequest request = pullRequestModel.getObject();
				return getSource().getRepository().git().log(request.getIntegrationInfo().getIntegrationBase(), 
						getSource().getHeadCommit(), null, 0, 0);
			}
			
		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		IModel<Repository> currentRepositoryModel = new LoadableDetachableModel<Repository>() {

			@Override
			protected Repository load() {
				RepositoryBasePage page = (RepositoryBasePage) getPage();
				return page.getRepository();
			}
			
		};
		
		add(new ComparableBranchSelector("target", currentRepositoryModel, targetModel) {

			@Override
			protected void onChange(AjaxRequestTarget target) {
				super.onChange(target);
				onBranchChange(target);
			}
			
		}.setRequired(true));
		
		add(new ComparableBranchSelector("source", currentRepositoryModel, sourceModel) {

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
				PullRequest request = pullRequestModel.getObject();
				if (request.getId() == null
						&& (request.getStatus() == PENDING_INTEGRATE || request.getStatus() == PENDING_APPROVAL) 
						&& request.getIntegrationInfo().getIntegrationHead() != null) {
					return "success";
				} else {
					return "warning";
				}
			}
			
		}));
		feedbackContainer.setOutputMarkupId(true);
		
		WebMarkupContainer messageContainer;
		feedbackContainer.add(messageContainer = new WebMarkupContainer("message") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = pullRequestModel.getObject();
				setVisible(request.getId() == null 
						&& (request.getStatus() == PENDING_APPROVAL 
							|| request.getStatus() == PENDING_INTEGRATE)); 
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
				PullRequest request = pullRequestModel.getObject();
				setVisible(request.getId() == null 
						&& (request.getStatus() == PENDING_APPROVAL || request.getStatus() == PENDING_INTEGRATE));
			}

			@Override
			public void onClick() {
				String title = getTitle();
				if (title != null) {
					PullRequestManager pullRequestManager = Gitop.getInstance(PullRequestManager.class);

					PullRequest request = pullRequestModel.getObject();
					
					if (request.getStatus() != INTEGRATED) {
						request.setAutoMerge(false);
						request.setTitle(title);
						request.setDescription(comment);
						
						pullRequestManager.send(request);
						
						setResponsePage(OpenRequestsPage.class, 
								PageSpec.forRepository(request.getTarget().getRepository()));
					}
				}
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				PullRequest request = pullRequestModel.getObject();
				if (request.getStatus() != PENDING_APPROVAL && request.getStatus() != PENDING_INTEGRATE)
					tag.put("disabled", "disabled");
			}
			
		});
		
		feedbackContainer.add(new Link<Void>("view") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(pullRequestModel.getObject().getId() != null);
			}

			@Override
			public void onClick() {
				PageParameters params = RequestDetailPage.params4(pullRequestModel.getObject());
				setResponsePage(RequestActivitiesPage.class, params);
			}
			
		});
		
		feedbackContainer.add(new Label("summary", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getTarget().equals(getSource())) {
					return "Please select different branches to pull.";
				} else {
					PullRequest request = pullRequestModel.getObject();
					if (request.getId() == null) {
						if (request.getStatus() == INTEGRATED) {
							return "No changes to pull.";
						} else if (request.getStatus() == PENDING_UPDATE) {
							return "Gate keeper of target repository rejects the pull request.";
						} else if (request.getIntegrationInfo().getIntegrationHead() == null) {
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
				PullRequest request = pullRequestModel.getObject();
				if (request.getId() != null) {
					return "#" + request.getId() + ": " + request.getTitle();
				} else if (!getTarget().equals(getSource())) {
					if (request.getStatus() == INTEGRATED) {
						return "Target branch '" + getTarget().getName() + "' of repository '" + getTarget().getRepository() 
								+ "' is already update to date.";
					} else if (title != null 
							&& request.getStatus() != PENDING_UPDATE 
							&& request.getIntegrationInfo().getIntegrationHead() == null) {
						return "But you can still send the pull request.";
					}
				} 
				return null;
			}
			
		}));
		
		feedbackContainer.add(new ListView<String>("rejectReasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return pullRequestModel.getObject().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				PullRequest request = pullRequestModel.getObject();
				setVisible(request.getId() == null && request.getStatus() == PENDING_UPDATE);
			}
			
		});
		
		IModel<Repository> repositoryModel = new AbstractReadOnlyModel<Repository>() {

			@Override
			public Repository getObject() {
				return getTarget().getRepository();
			}
			
		};
		add(commitsPanel = new CommitsTablePanel("commits", commitsModel, repositoryModel) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(pullRequestModel.getObject().getStatus() != INTEGRATED);
			}
			
		});
		commitsPanel.setOutputMarkupPlaceholderTag(true);
		
		add(diffViewPanel = new DiffViewPanel("changes", repositoryModel, new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return pullRequestModel.getObject().getIntegrationInfo().getIntegrationBase();
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
				
				setVisible(pullRequestModel.getObject().getStatus() != INTEGRATED);
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

	@Override
	public List<CommitComment> getCommitComments() {
		return new ArrayList<>();
	}

	@Override
	public boolean isShowInlineComments() {
		return false;
	}

	@Override
	public boolean canAddComments() {
		return false;
	}
}

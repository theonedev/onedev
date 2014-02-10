package com.pmease.gitop.web.page.project.pullrequest;

import java.util.Date;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.wicket.behavior.dropdown.DropdownBehavior;
import com.pmease.commons.wicket.behavior.dropdown.DropdownPanel;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.manager.VoteManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequest.Status;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.Vote;
import com.pmease.gitop.model.gatekeeper.voteeligibility.VoteEligibility;
import com.pmease.gitop.model.permission.ObjectPermission;
import com.pmease.gitop.web.component.label.AgeLabel;
import com.pmease.gitop.web.component.link.GitPersonLink;
import com.pmease.gitop.web.component.link.GitPersonLink.Mode;
import com.pmease.gitop.web.page.project.AbstractProjectPage;
import com.pmease.gitop.web.page.project.api.GitPerson;

@SuppressWarnings("serial")
public class RequestDetailPanel extends Panel {

	public RequestDetailPanel(String id, IModel<PullRequest> model) {
		super(id, model);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return getPullRequest().getTitle();
			}
			
		}));
		add(new Label("id", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return "#" + getPullRequest().getId();
			}
			
		}));
		add(new RequestStatusPanel("status", (IModel<PullRequest>) getDefaultModel()));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				
			}
			
		});

		add(new GitPersonLink("user", new LoadableDetachableModel<GitPerson>() {

			@Override
			protected GitPerson load() {
				User user = getPullRequest().getSubmitter();
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

		add(new Label("statusMessage", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				PullRequest request = getPullRequest();
				if (request.getStatus() == Status.PENDING_APPROVAL)
					return "This request has to be approved.";
				else 
					return "This request has to be updated.";
			}
			
		}) {

			@Override
			protected void onConfigure() {
				super.onConfigure();

				PullRequest request = getPullRequest();
				setVisible(request.getStatus() == Status.PENDING_APPROVAL 
						|| request.getStatus() == Status.PENDING_UPDATE);
			}
			
		});
		
		add(new ListView<String>("reasons", new LoadableDetachableModel<List<String>>() {

			@Override
			protected List<String> load() {
				return getPullRequest().getCheckResult().getReasons();
			}
			
		}) {

			@Override
			protected void populateItem(ListItem<String> item) {
				item.add(new Label("reason", item.getModelObject()));
			}

		});
		
		add(new Link<Void>("approve") {

			@Override
			public void onClick() {
				vote(Vote.Result.APPROVE);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				
				if (request.getStatus() == Status.PENDING_APPROVAL) {
					User currentUser = Gitop.getInstance(UserManager.class).getCurrent();
					if (currentUser != null) {
						boolean canVote = false;
						for (VoteEligibility each: request.getCheckResult().getVoteEligibilities()) {
							if (each.canVote(currentUser, request)) {
								canVote = true;
								break;
							}
						}
						setVisible(canVote);
					} else {
						setVisible(false);
					}
				} else {
					setVisible(false);
				}
			}
			
		});
		
		add(new Link<Void>("disapprove") {

			@Override
			public void onClick() {
				vote(Vote.Result.DISAPPROVE);
			}
			
		});

		WebMarkupContainer mergeContainer = new WebMarkupContainer("merge") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				PullRequest request = getPullRequest();
				setVisible(request.isOpen());
			}
			
		};
		mergeContainer.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (getPullRequest().getMergeResult().getMergeHead() != null)
					return "success";
				else
					return "warning";
			}
			
		}));
		add(mergeContainer);
		
		mergeContainer.add(new WebMarkupContainer("noConflicts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getMergeResult().getMergeHead() != null);
			}
			
		}); 
		
		WebMarkupContainer conflictsContainer = new WebMarkupContainer("conflicts") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getPullRequest().getMergeResult().getMergeHead() == null);
			}
			
		}; 
		mergeContainer.add(conflictsContainer);
		
		DropdownPanel helpDropdown = new DropdownPanel("helpDropdown") {

			@Override
			protected Component newContent(String id) {
				return new Fragment(id, "conflictHelpFrag", RequestDetailPanel.this);
			}
			
		};
		conflictsContainer.add(helpDropdown);
		conflictsContainer.add(new WebMarkupContainer("helpTrigger")
				.add(new DropdownBehavior(helpDropdown).clickMode(false)));
		
		mergeContainer.add(new Link<Void>("integrate") {

			@Override
			public void onClick() {
				Gitop.getInstance(PullRequestManager.class).merge(getPullRequest());
			}
			
		});
		
		mergeContainer.add(new Link<Void>("discard") {

			@Override
			public void onClick() {
				Gitop.getInstance(PullRequestManager.class).discard(getPullRequest());
			}
			
		});
	}
	
	private void vote(Vote.Result result) {
		Vote vote = new Vote();
		vote.setResult(Vote.Result.APPROVE);
		vote.setUpdate(getPullRequest().getLatestUpdate());
		vote.setVoter(Gitop.getInstance(UserManager.class).getCurrent());
		Gitop.getInstance(VoteManager.class).save(vote);		
		Gitop.getInstance(PullRequestManager.class).refresh(getPullRequest());
	}

	public PullRequest getPullRequest() {
		return (PullRequest) getDefaultModelObject();
	}

}
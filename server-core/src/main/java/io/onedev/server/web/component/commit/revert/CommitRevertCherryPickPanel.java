package io.onedev.server.web.component.commit.revert;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.web.component.branch.choice.BranchSingleChoice;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.pullrequests.detail.CommitMessageBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
abstract class CommitRevertCherryPickPanel extends Panel {

	private final IModel<Project> projectModel;

	private final String revision;

	private final CommitRevertCherryPickType type;

	private CommitMessageBean helperBean = new CommitMessageBean();

	private BranchSingleChoice baseChoice;

	private String baseBranch;

	public CommitRevertCherryPickPanel(String id, IModel<Project> projectModel, String revision, CommitRevertCherryPickType type) {
		super(id);
		this.projectModel = projectModel;
		this.revision = revision;
		this.type = type;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String oldCommitMessage = projectModel.getObject().getRevCommit(revision, true).getShortMessage();
		if (type == CommitRevertCherryPickType.REVERT) {
			helperBean.setCommitMessage("Revert \"" + oldCommitMessage + "\"" + "\n\nThis reverts commit " + revision);
		} else if (type == CommitRevertCherryPickType.CHERRY_PICK) {
			helperBean.setCommitMessage("Cherry Pick \"" + oldCommitMessage + "\"" + "\n\nThis cherry-pick commit " + revision);
		}
		baseBranch = projectModel.getObject().getDefaultBranch();

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new FencedFeedbackPanel("feedback", form));
		if (type == CommitRevertCherryPickType.REVERT) {
			form.add(new Label("title", "Revert Commit"));
		} else if (type == CommitRevertCherryPickType.CHERRY_PICK) {
			form.add(new Label("title", "Cherry-Pick Commit"));
		}
		BeanEditor editor;
		form.add(editor = BeanContext.edit("editor", helperBean));

		form.add(baseChoice = new BranchSingleChoice("baseBranch", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return baseBranch;
			}

			@Override
			public void setObject(String object) {
				baseBranch = object;
			}

		}, new LoadableDetachableModel<>() {

            @Override
            protected Map<String, String> load() {
                Map<String, String> branches = new LinkedHashMap<>();
                for (RefFacade ref : projectModel.getObject().getBranchRefs()) {
                    String branch = GitUtils.ref2branch(ref.getName());
                    branches.put(branch, branch);
                }
                return branches;
            }

        }, false) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				getSettings().setAllowClear(false);
			}

		});

		form.add(new AjaxButton("create") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				var project = projectModel.getObject();
				var branch = baseChoice.getModel().getObject();
				var user = SecurityUtils.getAuthUser();
				var commitMessage = helperBean.getCommitMessage();
				var protection = project.getBranchProtection(branch, user);
				RevCommit sourceHead = (RevCommit) project.getBranchRef("refs/heads/" + branch).getObj();
				RevCommit commitId = project.getRevCommit(revision, true);

				if (protection.isReviewRequiredForPush(project, sourceHead, commitId, new HashMap<>())){
					getSession().error("Review required for this change. Submit pull request instead");
					return;
				}
				var buildRequirement = protection.getBuildRequirement(project, sourceHead, commitId, new HashMap<>());
				if (!buildRequirement.getRequiredJobs().isEmpty()) {
					getSession().error("This change needs to be verified by some jobs. Submit pull request instead");
					return;
				}

				if (!project.isCommitSignatureRequirementSatisfied(user, branch, commitId)) {
					getSession().error("No valid signature for head commit of target branch");
					return;
				}

				if (protection.isCommitSignatureRequired()
						&& OneDev.getInstance(SettingManager.class).getGpgSetting().getSigningKey() == null) {
					getSession().error("Commit signature required but no GPG signing key specified");
					return;
				}
				var error = OneDev.getInstance(GitService.class).checkCommitMessages(protection, project, sourceHead, commitId, new HashMap<>());
				if (error != null) {
					getSession().error("Error validating commit message of '" + error.getCommitId().name() + "': " + error.getErrorMessage());
					return;
				}
				try {
					if (type == CommitRevertCherryPickType.REVERT) {
						OneDev.getInstance(GitService.class).revert(project, branch, revision, commitMessage, user.asPerson());
						onCreate(target, branch);
						getSession().success("Revert successfully");
					} else if (type == CommitRevertCherryPickType.CHERRY_PICK) {
						OneDev.getInstance(GitService.class).cherryPick(project, branch, revision, commitMessage, user.asPerson());
						onCreate(target, branch);
						getSession().success("Cherry Pick successfully");
					}
				} catch (Exception e) {
					ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
					if (explicitException != null) {
						getSession().error(explicitException.getMessage());
					} else {
						throw ExceptionUtils.unchecked(e);
					}
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		});

		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}

		});

		add(form);
	}

	protected abstract void onCreate(AjaxRequestTarget target, String branch);

	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	protected void onDetach() {
		projectModel.detach();

		super.onDetach();
	}

}

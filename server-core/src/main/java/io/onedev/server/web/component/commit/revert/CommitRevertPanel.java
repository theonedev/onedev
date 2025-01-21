package io.onedev.server.web.component.commit.revert;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.web.component.branch.choice.BranchSingleChoice;
import io.onedev.server.web.page.project.pullrequests.detail.CommitMessageBean;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import org.apache.wicket.model.LoadableDetachableModel;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
abstract class CommitRevertPanel extends Panel {

	private final IModel<Project> projectModel;

	private final String revision;

	private final Integer type;

	private CommitMessageBean helperBean = new CommitMessageBean();

	private BranchSingleChoice baseChoice;

	private String baseBranch;

	public CommitRevertPanel(String id, IModel<Project> projectModel, String revision, Integer type) {
		super(id);
		this.projectModel = projectModel;
		this.revision = revision;
		this.type = type;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		String oldCommitMessage = projectModel.getObject().getRevCommit(revision, true).getShortMessage();

		if (type == 0) {
			helperBean.setCommitMessage("Revert \"" + oldCommitMessage + "\"" + "\n\nThis reverts commit " + revision);
		} else if (type == 1) {
			helperBean.setCommitMessage("Cherry Pick \"" + oldCommitMessage + "\"" + "\n\nThis cherry-pick commit " + revision);
		}
		baseBranch = projectModel.getObject().getDefaultBranch();
		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		form.add(new FencedFeedbackPanel("feedback", form));

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

				Project project = projectModel.getObject();
				String branch = baseChoice.getModel().getObject();
				User user = SecurityUtils.getAuthUser();
				String commitMessage = helperBean.getCommitMessage();
				if (!project.isCommitSignatureRequirementSatisfied(user, branch, project.getRevCommit(revision, true))) {
					getSession().error("Valid signature required for head commit of this branch per branch protection rule");
				} else {
					try {
						if (type == 0) {
							OneDev.getInstance(GitService.class).revert(project, branch, revision, commitMessage, user.asPerson());
							onCreate(target, branch);
							getSession().success("Revert successfully");
						} else if (type == 1) {
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

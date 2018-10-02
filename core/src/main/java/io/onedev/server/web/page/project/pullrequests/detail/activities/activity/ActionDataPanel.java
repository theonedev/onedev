package io.onedev.server.web.page.project.pullrequests.detail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestActionManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestAction;
import io.onedev.server.model.User;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.page.project.pullrequests.detail.activities.SinceChangesLink;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class ActionDataPanel extends Panel {

	private static final String COMMENT_ID = "comment";
	
	private static final String DIFF_ID = "diff";
	
	public ActionDataPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		Fragment fragment;
		if (getAction().getData().getCommentSupport() != null || getAction().getData().getDiffSupport() != null) {
			fragment = new Fragment("content", "withCommentOrDiffFrag", this);
			if (getAction().getData().getCommentSupport() != null) {
				fragment.add(new ProjectCommentPanel(COMMENT_ID) {

					@Override
					protected String getComment() {
						return getAction().getData().getCommentSupport().getComment();
					}

					@Override
					protected void onSaveComment(AjaxRequestTarget target, String comment) {
						getAction().getData().getCommentSupport().setComment(comment);
						OneDev.getInstance(PullRequestActionManager.class).save(getAction());
					}

					@Override
					protected Project getProject() {
						return getAction().getRequest().getTargetProject();
					}

					@Override
					protected AttachmentSupport getAttachmentSupport() {
						return new ProjectAttachmentSupport(getProject(), getAction().getRequest().getUUID());
					}

					@Override
					protected boolean canModifyOrDeleteComment() {
						return SecurityUtils.canModifyOrDelete(getAction());
					}

					@Override
					protected String getRequiredLabel() {
						return null;
					}

					@Override
					protected ContentVersionSupport getContentVersionSupport() {
						return null;
					}

					@Override
					protected DeleteCallback getDeleteCallback() {
						return null;
					}
					
				});
			} else {
				fragment.add(new WebMarkupContainer(COMMENT_ID).setVisible(false));
			}
			DiffSupport diffSupport = getAction().getData().getDiffSupport();
			if (diffSupport != null) {
				fragment.add(new PlainDiffPanel(DIFF_ID, 
						diffSupport.getOldLines(), diffSupport.getOldFileName(),
						diffSupport.getNewLines(), diffSupport.getNewFileName(), 
						true));
			} else {
				fragment.add(new WebMarkupContainer(DIFF_ID).setVisible(false));
			}
		} else {
			fragment = new Fragment("content", "withoutCommentAndDiffFrag", this);
		}
		User user = User.getForDisplay(getAction().getUser(), getAction().getUserName());
		if (user != null)
			fragment.add(new UserLink("user", user));
		else
			fragment.add(new WebMarkupContainer("user").setVisible(false));
		
		fragment.add(new Label("action", getAction().getData().getDescription()));
		fragment.add(new Label("age", DateUtils.formatAge(getAction().getDate())));
		fragment.add(new SinceChangesLink("changes", new AbstractReadOnlyModel<PullRequest>() {

			@Override
			public PullRequest getObject() {
				return getAction().getRequest();
			}

		}, getAction().getDate()));
		
		add(fragment);
	}

	protected abstract PullRequestAction getAction();
	
}

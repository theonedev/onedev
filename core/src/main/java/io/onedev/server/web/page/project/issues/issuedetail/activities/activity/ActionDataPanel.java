package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueActionManager;
import io.onedev.server.model.IssueAction;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.DiffSupport;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.projectcomment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;
@SuppressWarnings("serial")
public abstract class ActionDataPanel extends Panel {

	private static final String DIFF_ID = "diff";
	
	private static final String COMMENT_ID = "comment";
	
	public ActionDataPanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DiffSupport diffSupport = getAction().getData().getDiffSupport();
		if (diffSupport != null)
			add(new PlainDiffPanel(DIFF_ID, diffSupport.getOldLines(), "a.txt", diffSupport.getNewLines(), "b.txt", true));
		else
			add(new WebMarkupContainer(DIFF_ID).setVisible(false));
		
		if (getAction().getData().getCommentSupport() != null) {
			add(new ProjectCommentPanel(COMMENT_ID) {

				@Override
				protected String getComment() {
					return getAction().getData().getCommentSupport().getComment();
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, String comment) {
					getAction().getData().getCommentSupport().setComment(comment);
					OneDev.getInstance(IssueActionManager.class).save(getAction());
				}

				@Override
				protected Project getProject() {
					return getAction().getIssue().getProject();
				}

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getAction().getIssue().getUUID());
				}

				@Override
				protected boolean canManageComment() {
					return SecurityUtils.canModify(getAction());
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
			add(new WebMarkupContainer(COMMENT_ID).setVisible(false));
		}
	}

	protected abstract IssueAction getAction();
	
}

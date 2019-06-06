package io.onedev.server.web.component.issue.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;
@SuppressWarnings("serial")
public abstract class DiffAndCommentAwarePanel extends Panel {

	private static final String DIFF_ID = "diff";
	
	private static final String COMMENT_ID = "comment";
	
	public DiffAndCommentAwarePanel(String id) {
		super(id);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		DiffSupport diffSupport = getDiffSupport();
		if (diffSupport != null)
			add(new PlainDiffPanel(DIFF_ID, diffSupport.getOldLines(), diffSupport.getOldFileName(), diffSupport.getNewLines(), diffSupport.getNewFileName(), true));
		else
			add(new WebMarkupContainer(DIFF_ID).setVisible(false));
		
		if (getChange().getData().getCommentSupport() != null) {
			add(new ProjectCommentPanel(COMMENT_ID) {

				@Override
				protected String getComment() {
					return getChange().getData().getCommentSupport().getComment();
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, String comment) {
					getChange().getData().getCommentSupport().setComment(comment);
					OneDev.getInstance(IssueChangeManager.class).save(getChange());
				}

				@Override
				protected Project getProject() {
					return getChange().getIssue().getProject();
				}

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getChange().getIssue().getUUID());
				}

				@Override
				protected boolean canModifyOrDeleteComment() {
					return SecurityUtils.canModifyOrDelete(getChange());
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

	protected abstract IssueChange getChange();
	
	protected abstract DiffSupport getDiffSupport();
}

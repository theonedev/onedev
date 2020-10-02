package io.onedev.server.web.component.issue.activities.activity;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueChangeManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.changedata.IssueFieldChangeData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.markdown.ContentVersionSupport;
import io.onedev.server.web.component.project.comment.ProjectCommentPanel;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;
import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.web.util.ProjectAttachmentSupport;
@SuppressWarnings("serial")
public abstract class IssueFieldChangePanel extends Panel {

	private final boolean hideNameIfOnlyOneRow;
	
	public IssueFieldChangePanel(String id, boolean hideNameIfOnlyOneRow) {
		super(id);
		this.hideNameIfOnlyOneRow = hideNameIfOnlyOneRow;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueFieldChangeData changeData = (IssueFieldChangeData) getChange().getData();
		
		add(new PropertyChangePanel("change", changeData.getOldFieldValues(), changeData.getNewFieldValues(), 
				hideNameIfOnlyOneRow));
		
		if (changeData.getCommentAware() != null) {
			add(new ProjectCommentPanel("comment") {

				@Override
				protected String getComment() {
					return getChange().getData().getCommentAware().getComment();
				}

				@Override
				protected List<User> getMentionables() {
					return OneDev.getInstance(UserManager.class).queryAndSort(getChange().getIssue().getParticipants());
				}

				@Override
				protected void onSaveComment(AjaxRequestTarget target, String comment) {
					getChange().getData().getCommentAware().setComment(comment);
					OneDev.getInstance(IssueChangeManager.class).save(getChange());
				}

				@Override
				protected Project getProject() {
					return getChange().getIssue().getProject();
				}

				@Override
				protected AttachmentSupport getAttachmentSupport() {
					return new ProjectAttachmentSupport(getProject(), getChange().getIssue().getUUID(), 
							SecurityUtils.canManageIssues(getProject()));
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
			add(new WebMarkupContainer("comment").setVisible(false));
		}
		
	}
	
	protected abstract IssueChange getChange();
	
}

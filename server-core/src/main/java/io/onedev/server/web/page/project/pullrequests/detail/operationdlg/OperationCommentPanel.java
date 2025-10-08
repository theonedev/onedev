package io.onedev.server.web.page.project.pullrequests.detail.operationdlg;

import com.google.common.collect.Lists;
import io.onedev.server.attachment.AttachmentSupport;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.comment.CommentInput;
import io.onedev.server.web.component.modal.ModalPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.model.PropertyModel;

import org.jspecify.annotations.Nullable;
import java.util.List;

public abstract class OperationCommentPanel extends ObsoleteUpdateAwarePanel {

	private String comment;
	
	public OperationCommentPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
		super(componentId, modal, latestUpdateId);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getForm().add(new CommentInput("comment", new PropertyModel<String>(this, "comment"), false) {

			@Override
			protected Project getProject() {
				return getLatestUpdate().getRequest().getTargetProject();
			}
			
			@Override
			protected AttachmentSupport getAttachmentSupport() {
				return new ProjectAttachmentSupport(getProject(), getLatestUpdate().getRequest().getUUID(), 
						SecurityUtils.canManagePullRequests(getProject()));
			}

			@Override
			protected List<User> getParticipants() {
				return getLatestUpdate().getRequest().getParticipants();
			}
			
			@Override
			protected List<Behavior> getInputBehaviors() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a note"));
			}
			
		});
	}

	@Nullable
	protected final String getComment() {
		return comment;
	}

}

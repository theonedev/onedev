package io.onedev.server.web.page.project.pullrequests.detail.operationconfirm;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.PropertyModel;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.markdown.AttachmentSupport;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.project.comment.CommentInput;
import io.onedev.server.web.util.ProjectAttachmentSupport;

@SuppressWarnings("serial")
public abstract class CommentableOperationConfirmPanel extends OperationConfirmPanel {

	private String comment;
	
	public CommentableOperationConfirmPanel(String componentId, ModalPanel modal, Long latestUpdateId) {
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
			protected List<User> getMentionables() {
				return OneDev.getInstance(UserManager.class)
						.queryAndSort(getLatestUpdate().getRequest().getParticipants());
			}
			
			@Override
			protected List<AttributeModifier> getInputModifiers() {
				return Lists.newArrayList(AttributeModifier.replace("placeholder", "Leave a note"));
			}
			
		});
	}

	@Nullable
	protected final String getComment() {
		return comment;
	}
	
}

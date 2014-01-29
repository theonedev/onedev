package com.pmease.gitop.web.page.project.source.commit.diff.renderer;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.model.CommitComment;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.web.common.wicket.bootstrap.Alert;
import com.pmease.gitop.web.page.project.source.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class BlobMessagePanel extends BlobDiffPanel {

	private final IModel<String> messageModel;
	
	public BlobMessagePanel(String id,
			int index,
			IModel<FileHeader> fileModel,
			IModel<Project> projectModel, 
			IModel<String> sinceModel,
			IModel<String> untilModel, 
			IModel<List<CommitComment>> commentsModel,
			IModel<String> messageModel) {
		super(id, index, fileModel, projectModel, sinceModel, untilModel, commentsModel);
		
		this.messageModel = messageModel;
	}

	@Override
	protected Component createActionsBar(String id) {
		return new WebMarkupContainer(id).setVisibilityAllowed(false);
	}

	@Override
	protected Component createDiffContent(String id) {
		Alert alert = new Alert(id, messageModel);
		alert.type(Alert.Type.Warning).setCloseButtonVisible(false);
		alert.withHtmlMessage(true);
		return alert;
	}
	
	@Override
	public void onDetach() {
		if (messageModel != null) {
			messageModel.detach();
		}
		
		super.onDetach();
	}
}

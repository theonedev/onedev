package com.pmease.gitplex.web.page.repository.code.commit.diff.renderer;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.common.wicket.bootstrap.Alert;
import com.pmease.gitplex.web.page.repository.code.commit.diff.patch.FileHeader;

@SuppressWarnings("serial")
public class BlobMessagePanel extends BlobDiffPanel {

	private final IModel<String> messageModel;
	
	public BlobMessagePanel(String id,
			int index,
			IModel<Repository> repoModel,
			IModel<FileHeader> fileModel,
			String sinceRevision,
			String untilRevision, 
			IModel<String> messageModel) {
		super(id, index, repoModel, fileModel, sinceRevision, untilRevision);
		
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
		if (messageModel != null) 
			messageModel.detach();
		
		super.onDetach();
	}
}

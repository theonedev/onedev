package io.onedev.server.web.page.project.blob.render.commitoption;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ReferenceAware;
import io.onedev.server.util.HierarchicalContext;

@Editable
public class CommitMessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;
	
	@Editable(order=100, name="Commit Message", placeholderProvider = "getDefaultCommitMessage")
	@Multiline
	@OmitName
	@ReferenceAware
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
	@SuppressWarnings("unused")
	private static String getDefaultCommitMessage() {
		return HierarchicalContext.get().findData(CommitOptionPanel.class).getDefaultCommitMessage();
	}
	
}

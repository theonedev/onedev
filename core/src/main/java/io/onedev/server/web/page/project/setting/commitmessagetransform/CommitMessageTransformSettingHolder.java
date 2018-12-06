package io.onedev.server.web.page.project.setting.commitmessagetransform;

import java.io.Serializable;

import io.onedev.server.model.support.CommitMessageTransformSetting;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class CommitMessageTransformSettingHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private CommitMessageTransformSetting commitMessageTransformSetting;

	@Editable(name="Enable")
	public CommitMessageTransformSetting getCommitMessageTransformSetting() {
		return commitMessageTransformSetting;
	}

	public void setCommitMessageTransformSetting(CommitMessageTransformSetting commitMessageTransformSetting) {
		this.commitMessageTransformSetting = commitMessageTransformSetting;
	}

}

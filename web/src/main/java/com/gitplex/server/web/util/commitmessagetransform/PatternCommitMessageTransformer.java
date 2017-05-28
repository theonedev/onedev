package com.gitplex.server.web.util.commitmessagetransform;

import javax.inject.Singleton;

import com.gitplex.server.model.Project;
import com.gitplex.server.model.support.CommitMessageTransformSetting;

@Singleton
public class PatternCommitMessageTransformer implements CommitMessageTransformer {

	@Override
	public String transform(Project project, String commitMessage) {
		CommitMessageTransformSetting setting = project.getCommitMessageTransformSetting();
		if (setting != null) {
			return commitMessage.replaceAll(setting.getSearchFor(), setting.getReplaceWith());
		} else {
			return commitMessage;
		}
	}

}

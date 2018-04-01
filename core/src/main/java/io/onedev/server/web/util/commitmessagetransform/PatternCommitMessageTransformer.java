package io.onedev.server.web.util.commitmessagetransform;

import javax.inject.Singleton;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.CommitMessageTransformSetting;

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

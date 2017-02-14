package com.gitplex.server.core.commitmessagetransform;

import javax.inject.Singleton;

import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.support.CommitMessageTransformSetting;

@Singleton
public class PatternCommitMessageTransformer implements CommitMessageTransformer {

	@Override
	public String transform(Depot depot, String commitMessage) {
		CommitMessageTransformSetting setting = depot.getCommitMessageTransformSetting();
		if (setting != null) {
			return commitMessage.replaceAll(setting.getSearchFor(), setting.getReplaceWith());
		} else {
			return commitMessage;
		}
	}

}

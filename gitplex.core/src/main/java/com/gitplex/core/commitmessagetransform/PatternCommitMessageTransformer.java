package com.gitplex.core.commitmessagetransform;

import javax.inject.Singleton;

import com.gitplex.core.entity.Depot;
import com.gitplex.core.entity.support.CommitMessageTransformSetting;

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

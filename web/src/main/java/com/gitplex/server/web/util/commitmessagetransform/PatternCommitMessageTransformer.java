package com.gitplex.server.web.util.commitmessagetransform;

import javax.inject.Singleton;

import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.CommitMessageTransformSetting;

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

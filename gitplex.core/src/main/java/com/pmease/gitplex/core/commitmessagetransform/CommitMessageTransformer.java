package com.pmease.gitplex.core.commitmessagetransform;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.entity.Depot;

@ExtensionPoint
public interface CommitMessageTransformer {
	
	String transform(Depot depot, String commitMessage);
	
}
